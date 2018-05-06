package com.tsys.digital.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsys.digital.domain.Location;
import com.tsys.digital.domain.LocationType;
import com.tsys.digital.services.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.beans.PropertyEditor;

// PathParams Vs QueryParams
// PathParam leads upto the entity/resource type that you are requesting.
// if the parameter identifies a specific entity you should use a path variable.
// to get the post with id = 123, I would request
//
// GET: /posts/123
//
// but to filter my list of posts, and get all posts since Jan 1, 2013,
// I would request using QueryParams
//
// GET: /posts?since=2013-01-01
// Similarly use QueryParams for pagination, sorting etc...
//
//
// /Invoices             // all invoices
// /Invoices?after=2011  // a filter on all invoices
//
// /Invoices/52          // by 52
// /Invoices/52/Items    // all items on invoice 52
// /Invoices/52/Items/1  // Item 1 from invoice 52
//
// /Companies/{company}/Invoices?sort=Date
// /Companies/{company}/Invoices/{invoiceNo} // assuming that the invoice only unq by company?
//
// Many a times optional parameters are also easier to put as query params.
//
//
// Another Perspective from Caching requests at web server end:
//
// You can use both of them, there's not any strict rule about this
// subject, but using URI path variables has some advantages:
//
// Cache: Most of the web cache services on the internet don't
// cache GET request when they contains query parameters. They do
// that because there are a lot of RPC systems using GET requests
// to change data in the server (fail!! Get must be a safe method)
//
// But if you use path variables, all of this services can cache your
// GET requests.
//
// Hierarchy: The path variables can represent hierarchy: /City/Street/Place
//
// It gives the user more information about the structure of the data.
//
// But if your data doesn't have any hierarchy relation you can still
// use Path variables, using comma or semi-colon:
//
//    /City/longitude,latitude
//
// As a rule, use comma when the ordering of the parameters matter,
// use semi-colon when the ordering doesn't matter:
//
//   /IconGenerator/red;blue;green
//

@RestController
@RequestMapping("/locations")
public class LocationController {
  private static final Logger LOG = LoggerFactory.getLogger(LocationController.class);
  private final LocationService locationService;
  private final PropertyEditor metricEnumConverter;
  private final PropertyEditor sortDirectionEnumConverter;
  private final ObjectMapper jackson2;

  // https://machiel.me/post/java-enums-as-request-parameters-in-spring-4/
  // scope: This is called per-request
  @InitBinder
  public void initBinder(WebDataBinder dataBinder) {
    dataBinder.registerCustomEditor(Metric.class, this.metricEnumConverter);
    dataBinder.registerCustomEditor(Sort.Direction.class, this.sortDirectionEnumConverter);
  }

  @Autowired
  LocationController(LocationService locationService, PropertyEditor metricEnumConverter, PropertyEditor sortDirectionEnumConverter, ObjectMapper jackson2) {
    this.metricEnumConverter = metricEnumConverter;
    this.locationService = locationService;
    this.sortDirectionEnumConverter = sortDirectionEnumConverter;
    this.jackson2 = jackson2;
  }

  @GetMapping("health")
  public String index() {
    return String.format("{ 'ok' : %s }", LocationController.class.getSimpleName());
  }

  @PostMapping
  public ResponseEntity<Location> add(Location location) {
    return new ResponseEntity<>(locationService.saveOrUpdate(location), HttpStatus.CREATED);
  }

//  @PostMapping
//  public ResponseEntity<String> add(Location location) {
//    final Location saved = locationService.saveOrUpdate(location);
//    return new ResponseEntity<>(saved.toJson(), HttpStatus.CREATED);
//  }

  @GetMapping("{id}")
  public ResponseEntity<String> findById(@PathVariable String id) {
    return locationService.findById(id)
        .map(location -> {
          try {
            return new ResponseEntity<>(jackson2.writeValueAsString(location), HttpStatus.OK);
          } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(String.format("{ 'error' : 'json processing: %s' }", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
          }
        })
        .orElse(new ResponseEntity<>("{ 'error' : 'not found' }", HttpStatus.NOT_FOUND));
  }

  @GetMapping("count")
  public ResponseEntity<String> total() {
    return new ResponseEntity<>(
        String.format("{ 'count' : %d }", locationService.count()),
        HttpStatus.OK);
  }

  // PUT Vs. PATCH

  @PutMapping
  public Location update(@RequestBody String id,
                         @RequestBody String name,
                         @RequestBody LocationType type) {
    System.out.println("id = " + id);
    System.out.println("name = " + name);
    System.out.println("type = " + type);
    return null;
//    System.out.println("location = " + location);
//    return locationService.saveOrUpdate(location);
  }

  @DeleteMapping("{id}")
  public ResponseEntity<String> deleteById(@PathVariable String id) {
    Boolean result = locationService.deleteById(id);
    return new ResponseEntity<>(
        String.format("{ 'deleted' : '%s' }", result),
        HttpStatus.OK);
  }

  @DeleteMapping
  public ResponseEntity<HttpStatus> deleteAll() {
    locationService.deleteAll();
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("near")
  public ResponseEntity<GeoResults<RedisGeoCommands.GeoLocation<Location>>>
  locationsNearby(@RequestParam("longitude") double longitude,
                  @RequestParam("latitude") double latitude,
                  @RequestParam(value = "within", required = false, defaultValue = "1") double radius,
                  @RequestParam(value = "metric", required = false, defaultValue = "") Metric metric) {
    return new ResponseEntity<>(locationService.locationsNearby(
        longitude, latitude, new Distance(radius, metric)), HttpStatus.OK);
  }

  // The Spring framework provides an out-of-the-box feature for pagination
  // that needs the number of pages and the number of elements per page.
  // This is very useful when we would implement "static pagination," which
  // can offer a user to choose between different pages.
  //
  // 2. Page as Resource vs Page as Representation
  // The first question when designing pagination in the context of a RESTful
  // architecture is whether to consider the page an actual Resource or just
  // a Representation of Resources.
  //
  // Treating the page itself as a resource introduces a host of problems such
  // as no longer being able to uniquely identify resources between calls.
  // This, coupled with the fact that, in the persistence layer, the page is
  // not proper entity but a holder that is constructed when necessary, makes
  // the choice straightforward: the page is part of the representation.
  //
  // The next question in the pagination design in the context of REST is
  // where to include the paging information:
  //
  //  in the URI path: /foo/page/1/size/10/sort/ascending
  //  the URI query: /foo?page=1&size=10&sort=ascending
  //
  // Keeping in mind that a page is not a Resource, encoding the page information
  // in the URI is no longer an option.
  //
  // We are going to use the standard way of solving this problem by encoding
  // the paging information in a URI query.
  //
  //
  // Type of Pagination:
  // 1. Static Pagination - is the one described above
  // 2. Dynamic Pagination -  is where the content is loaded automatically as
  // we scroll down.  For e.g - loading more tweets as we scroll down.
  @GetMapping()
  public ResponseEntity<Page<Location>>
  findAll(@RequestParam(value = "page", required = false, defaultValue = "0") int page,
          @RequestParam(value = "size", required = false, defaultValue = "0") int size,
          @RequestParam(value = "sortDirection", required = false, defaultValue = "asc") Sort.Direction sortDirection,
          @RequestParam(value = "sortBy", required = false, defaultValue = "id") String sortBy) {
    System.out.println("page = " + page);
    System.out.println("size = " + size);
    System.out.println("sortDirection = " + sortDirection);
    final Pageable pageable = (size < 1) ? Pageable.unpaged() : PageRequest.of(page, size, sortDirection, sortBy);
    return new ResponseEntity<>(locationService.findAll(pageable), HttpStatus.OK);
  }

  @GetMapping("distance")
  public Distance distanceBetween(@RequestParam String id1,
                                  @RequestParam String id2,
                                  @RequestParam(name = "in", required = false, defaultValue = "kms") Metric metric) {
    return locationService.distanceBetween(id1, id2, metric);
  }
}