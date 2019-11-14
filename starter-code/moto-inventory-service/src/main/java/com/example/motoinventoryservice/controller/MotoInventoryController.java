package com.example.motoinventoryservice.controller;

import com.example.motoinventoryservice.dao.MotoInventoryDao;
import com.example.motoinventoryservice.model.Motorcycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


@RestController
@RefreshScope
public class MotoInventoryController {
    @Autowired
    private MotoInventoryDao motoDAO;

    @Autowired
    private DiscoveryClient discoveryClient;

    private RestTemplate restTemplate = new RestTemplate();

    @Value("${motoServiceName}")
    private String motoServiceName;

    @Value("${serviceProtocol}")
    private String serviceProtocol;

    @Value("${servicePath}")
    private String servicePath;
//
//    @Value("${officialGreeting}")
//    private String officialGreeting;

    @RequestMapping(value = "/vehicles/{vin}", method = RequestMethod.GET)
    public HashMap getVehicle(@PathVariable String vin) {

        List<ServiceInstance> instances = discoveryClient.getInstances(motoServiceName);

        String motoServiceUri = serviceProtocol + instances.get(0).getHost() + ":" + instances.get(0).getPort() + servicePath + vin;

        Motorcycle motorcycle = restTemplate.getForObject(motoServiceUri, Motorcycle.class);

        HashMap<String, String> motoMap = new HashMap<String, String>(){
            {
                put("Vehicle Type", "Motorcycle");
                put("Vehicle Make", motorcycle.getMake());
                put("Vehicle Model", motorcycle.getModel());
                put("Vehicle Color", motorcycle.getColor());
                put("Vehicle Year", motorcycle.getYear());

            }};


        return motoMap;
    }


    @RequestMapping(value = "/motorcycles", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    public Motorcycle createMotorcycle(@RequestBody @Valid Motorcycle motorcycle) {
        motoDAO.addMotorcycle(motorcycle);

        return motorcycle;
    }

    @RequestMapping(value = "/motorcycles/{motoId}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public Motorcycle getMotorcycle(@PathVariable int motoId) {
        if (motoId < 1) {
           throw new IllegalArgumentException("MotoId must be greater than 0.");
        }

        return motoDAO.getMotorcycle(motoId);
    }

    @RequestMapping(value = "/motorcycles/{motoId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMotorcycle(@PathVariable("motoId") int motoId) {
        motoDAO.deleteMotorcycle(motoId);
    }

    @RequestMapping(value = "/motorcycles/{motoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMotorcycle(@RequestBody @Valid Motorcycle motorcycle, @PathVariable int motoId) {
        // make sure the motoId on the path matches the id of the motorcycle object
        if (motoId != motorcycle.getId()) {
            throw new IllegalArgumentException("Motorcycle ID on path must match the ID in the Motorcycle object.");
        }

        motoDAO.updateMotorcycle(motorcycle);
    }
}