package com.example.AjiraNet.controller;

import com.example.AjiraNet.model.Devices;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * API to find route between two devices in a network
 * @author Saranya Kumar
 */
@RestController
@RequestMapping("/ajiranet/process")
public class DeviceController {

    static Map<String,String> deviceMap = new HashMap<String,String>();
    static Map<String,Integer> deviceStrengthMap = new HashMap<String,Integer>();
    static Map<String,Integer> deviceConnectionMap = new HashMap<String,Integer>();
    static int deviceCount = 0;
    static int DEFAULT_STRENGTH = 5;
    static ArrayList<ArrayList<Integer>> edges = new ArrayList<ArrayList<Integer>>();

    /*
     * This method is to add devices
     * @Param devices model class containing device name and type
     * @Return ResponseEntity which contains HttpStatus code and message
     */
    @PostMapping(value = "CREATE/devices")
    public ResponseEntity<String> addDevice(@RequestBody Devices devices) {
        try {
            if (devices.getType().isEmpty() || devices.getName().isEmpty()) {
                // Validation for empty device name or type
                return new ResponseEntity<String>("HTTP Response Code: " + HttpStatus.BAD_REQUEST +
                        "\nResponse: {\"msg\": \"Device Type or Name cannot be empty\"}",
                        HttpStatus.BAD_REQUEST);
            } else if (!(devices.getType().equals("COMPUTER") || devices.getType().equals("REPEATER"))) {
                // Validation for device type to be an enumeration of either ‘COMPUTER’ or ‘REPEATER’
                return new ResponseEntity<String>("HTTP Response Code: " + HttpStatus.BAD_REQUEST +
                        "\nResponse: {\"msg\": \"type '" + devices.getType() + "' is not supported\"}",
                        HttpStatus.BAD_REQUEST);
            } else if (deviceMap.containsKey(devices.getName())) {
                // Validation for device whether it is already added
                return new ResponseEntity<String>("HTTP Response Code: " + HttpStatus.BAD_REQUEST +
                        "\nResponse: {\"msg\": \"Device '" + devices.getName() + "' already exists\"}",
                        HttpStatus.BAD_REQUEST);
            } else {
                // Adding devices
                deviceMap.put(devices.getName(), devices.getType());
                deviceConnectionMap.put(devices.getName(), deviceCount++);
                edges.add(new ArrayList<Integer>());
                return new ResponseEntity<String>("HTTP Response Code: " + HttpStatus.OK +
                        "\nResponse: {\"msg\": \"Successfully added " + devices.getName() + "\"}",
                        HttpStatus.OK);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<String>("HTTP Response Code: " + HttpStatus.BAD_REQUEST +
                    "\nResponse: {\"msg\": \"Invalid Command.\"}",
                    HttpStatus.BAD_REQUEST);
        }
    }

    /*
     * This method is to create connection between two devices
     * @Param devices model class containing source and target device name
     * @Return ResponseEntity which contains HttpStatus code and message
     */
    @PostMapping(value = "CREATE/connections")
    public ResponseEntity<String> createConnections(@RequestBody Devices devices) {
        try {
            String source = devices.getSource();
            String[] destArray = devices.getTargets();
            if(!deviceMap.containsKey(source)){
                // Validation for whether given source is present in network
                return new ResponseEntity<String>("HTTP Response Code: " + HttpStatus.BAD_REQUEST +
                        "\nResponse: {\"msg\": \"Node '"+ source + "' not found\"}",
                        HttpStatus.BAD_REQUEST);
            } else {
                // Validation for whether given destination is present in network
                for (String dest : destArray){
                    if(!deviceMap.containsKey(dest)){
                        return new ResponseEntity<String>("HTTP Response Code: " + HttpStatus.BAD_REQUEST +
                                "\nResponse: {\"msg\": \"Node '"+ dest + "' not found\"}",
                                HttpStatus.BAD_REQUEST);
                    }
                }
            }
            if (Arrays.asList(destArray).contains(source)) {
                // Validation for whether device is connected to itself
                return new ResponseEntity<String>("HTTP Response Code: " + HttpStatus.BAD_REQUEST +
                        "\nResponse: {\"msg\": \"Cannot connect device to itself\"}",
                        HttpStatus.BAD_REQUEST);
            }
            int src = deviceConnectionMap.get(source);
            for(String destString : destArray) {
                int dest = deviceConnectionMap.get(destString);
                if (edges.get(src).contains(dest) || edges.get(dest).contains(src)) {
                    // Validation for whether the given source and target device are already connected
                    return new ResponseEntity<String>("HTTP Response Code: " + HttpStatus.BAD_REQUEST +
                            "\nResponse: {\"msg\": \"Devices are already connected\"}",
                            HttpStatus.BAD_REQUEST);
                }
            }
            for(String destString : destArray){
                int dest = deviceConnectionMap.get(destString);
                edges.get(src).add(dest);
                edges.get(dest).add(src);
            }
            deviceMap.put(devices.getName(), devices.getType());
            // Successfully creating connection between source and target devices
            return new ResponseEntity<String>("HTTP Response Code: " + HttpStatus.OK +
                    "\nResponse: {\"msg\": \"Successfully connected\"}",
                    HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<String>("HTTP Response Code: " + HttpStatus.BAD_REQUEST +
                    "\nResponse: {\"msg\": \"Invalid command syntax\"}",
                    HttpStatus.BAD_REQUEST);
        }
    }

    /*
     * This method returns all the devices in network
     * @Return ResponseEntity which contains HttpStatus code and message containing all devices
     */
    @PostMapping(value = "FETCH/devices")
    public ResponseEntity<String> fetchDevices(){
        try{
            String devices = "";
            if(!deviceMap.isEmpty()){
                // Validation for presence of devices in network
                devices += "\"devices\": [";
                Set<String> keySet = deviceMap.keySet();
                for(String key : keySet){
                    devices += "\n\t{" + "\n\t\t'type': '" + deviceMap.get(key) + "',\n\t\t'name': '" + key + "'\n\t},";
                }
                devices = devices.substring(0,devices.length()-1);
                devices += "\n]";
            } else{
                // Validation for absence of devices in network
                devices += "\"devices\": []";
            }
            return new ResponseEntity<String>("HTTP Response Code: "+HttpStatus.OK +
                    "\nResponse: \n{\n" + devices + "\n}",
                    HttpStatus.OK);
        } catch(Exception e){
            return new ResponseEntity<String>("HTTP Response Code: " + HttpStatus.BAD_REQUEST +
                    "\nResponse: {\"msg\": \"Invalid Command.\"}",
                    HttpStatus.BAD_REQUEST);
        }

    }

    /*
     * This method returns route between from and to device
     * @Param from device and to device
     * @Return ResponseEntity which contains HttpStatus code and message containing route between two devices
     */
    @PostMapping(value = "FETCH/info-routes")
    @ResponseBody
    public ResponseEntity<String> fetchInfoRoute(@RequestParam(required = false) String from, @RequestParam(required = false) String to) {
        try {
            if (from.isEmpty() || to.isEmpty()) {
                // Validation for whether from or to device empty
                return new ResponseEntity<String>("HTTP Response Code: " + HttpStatus.BAD_REQUEST +
                        "\nResponse: {\"msg\": \"Invalid Request\"}",
                        HttpStatus.BAD_REQUEST);
            } else if (!deviceMap.containsKey(from)) {
                // Validation for presence of from device in network
                return new ResponseEntity<String>("HTTP Response Code: " + HttpStatus.BAD_REQUEST +
                        "\nResponse: {\"msg\": \"Node '" + from + "' not found\"}",
                        HttpStatus.BAD_REQUEST);
            } else if (!deviceMap.containsKey(to)) {
                // Validation for presence of to device in network
                return new ResponseEntity<String>("HTTP Response Code: " + HttpStatus.BAD_REQUEST +
                        "\nResponse: {\"msg\": \"Node '" + to + "' not found\"}",
                        HttpStatus.BAD_REQUEST);
            } else if (deviceMap.get(to).equals("REPEATER") || deviceMap.get(to).equals("REPEATER")) {
                // Validation for either type of from or to device is REPEATER
                return new ResponseEntity<String>("HTTP Response Code: " + HttpStatus.BAD_REQUEST +
                        "\nResponse: {\"msg\": \"Route cannot be calculated with repeater\"}",
                        HttpStatus.BAD_REQUEST);
            } else if (from.equals(to)) {
                // Validation for routebetween same device
                return new ResponseEntity<String>("HTTP Response Code: " + HttpStatus.OK +
                        "\nResponse: {\"msg\": \"Route is " + from + " -> " + to + " \"}",
                        HttpStatus.BAD_REQUEST);
            } else {
                int src = deviceConnectionMap.get(from);
                int dest = deviceConnectionMap.get(to);
                String route = printShortestDistance(edges, src, dest, deviceCount);
                if(route.equals("")) {
                    // Validation for absence of route between devices
                    return new ResponseEntity<String>("HTTP Response Code: " + HttpStatus.NOT_FOUND +
                            "\nResponse: {\"msg\": \"Route not found\"}",
                            HttpStatus.NOT_FOUND);
                } else if(route.equals("WeakSignalStrength")){
                    // Validation for insufficient signal strength to transfer information between from and to devices
                    return new ResponseEntity<String>("HTTP Response Code: " + HttpStatus.NOT_FOUND +
                            "\nResponse: {\"msg\": \"Signal Strength not sufficient to transfer information \"}",
                            HttpStatus.NOT_FOUND);
                }else {
                    // Returning route between from and to devices
                    return new ResponseEntity<String>("HTTP Response Code: " + HttpStatus.OK +
                            "\nResponse: {\"msg\": \"Route is " + route + "\"}",
                            HttpStatus.OK);
                }
            }
        } catch (Exception e) {
            return new ResponseEntity<String>("HTTP Response Code: " + HttpStatus.BAD_REQUEST +
                     "\nResponse: {\"msg\": \"Invalid Request\"}",
                    HttpStatus.BAD_REQUEST);
        }
    }

    /*
     * This method returns shortest route between from and to device
     * @Param connection list, from, to and total devices in network
     * @Return String containg shortest route between two devices
     */
    private static String printShortestDistance(ArrayList<ArrayList<Integer>> adjEdges, int src, int dest, int deviceCnt)
    {
        String route = "";
        int pred[] = new int[deviceCnt];

        if (BFS(adjEdges, src, dest, deviceCnt, pred) == false) {
            return route;
        }

        LinkedList<Integer> path = new LinkedList<Integer>();
        int crawl = dest;
        path.add(crawl);
        while (pred[crawl] != -1) {
            path.add(pred[crawl]);
            crawl = pred[crawl];
        }
        int signalStrength;
        String from = getKey(src);
        if(deviceStrengthMap.containsKey(from)){
            signalStrength = deviceStrengthMap.get(from);
        } else{
            signalStrength = DEFAULT_STRENGTH;
        }
        for (int index = path.size() - 1; index > 0; index--) {
            String deviceName =getKey(path.get(index));
            if(deviceMap.get(deviceName).equals("COMPUTER")){
                signalStrength--;
            } else{
                signalStrength = signalStrength * 2;
            }
            route += deviceName + " -> ";
        }
        route += getKey(path.get(0));
        if(signalStrength < 0){
            route = "WeakSignalStrength";
        }
        return route;
    }

    /*
     * This method returns key for respective value
     * @Param value
     * @Return String which is key for the given value
     */
    private static String getKey(int value){
        for(String key : deviceConnectionMap.keySet()){
            if(value == deviceConnectionMap.get(key))
                return key;
        }
        return "";
    }

    /*
     * This method using Breadth first search to find shortest route
     * @Param connection list, from, to, total number of devices and parent array
     * @Return Boolean defining whether there is any route between source and destination
     */
    private static boolean BFS(ArrayList<ArrayList<Integer>> adjEdges, int src, int dest, int deviceCnt, int pred[])
    {
        LinkedList<Integer> queue = new LinkedList<Integer>();
        boolean visited[] = new boolean[deviceCnt];
        for (int index = 0; index < deviceCnt; index++) {
            visited[index] = false;
            pred[index] = -1;
        }
        visited[src] = true;
        queue.add(src);

        // bfs Algorithm
        while (!queue.isEmpty()) {
            int element = queue.remove();
            for (int index = 0; index < adjEdges.get(element).size(); index++) {
                if (visited[adjEdges.get(element).get(index)] == false) {
                    visited[adjEdges.get(element).get(index)] = true;
                    pred[adjEdges.get(element).get(index)] = element;
                    queue.add(adjEdges.get(element).get(index));
                    if (adjEdges.get(element).get(index) == dest) {
                        // Destination reached
                        return true;
                    }
                }
            }
        }
        // No route found
        return false;
    }

    /*
     * This method is to change signal strength of device
     * @Param device name and devices model class containing strength value
     * @Return ResponseEntity which contains HttpStatus code and message
     */
    @RequestMapping(value = "MODIFY/devices/{name}/strength", method = RequestMethod.POST)
    public ResponseEntity<String> modifyStrength(@PathVariable("name") String name, @RequestBody Devices devices){
        try{
            if(name.isEmpty() || !deviceMap.containsKey(name)){
                // Validation for invalid device name
                return new ResponseEntity<String>("HTTP Response Code: "+HttpStatus.NOT_FOUND +
                        "\nResponse: {\"msg\": \"Device Not Found\"}",
                        HttpStatus.NOT_FOUND);
            } else {
                try{
                    // Defining strength for a device
                    deviceStrengthMap.put(name, Integer.parseInt(devices.getValue()));
                    return new ResponseEntity<String>("HTTP Response Code: "+HttpStatus.OK +
                            "\nResponse: {\"msg\": \"Successfully defined strength\"}",
                            HttpStatus.OK);
                } catch (NumberFormatException e1){
                    // Validation for strength value
                    return new ResponseEntity<String>("HTTP Response Code: "+HttpStatus.BAD_REQUEST +
                            "\nResponse: {\"msg\": \"value should be an integer\"}",
                            HttpStatus.OK);
                }

            }
        } catch(Exception e){
            return new ResponseEntity<String>("HTTP Response Code: " + HttpStatus.BAD_REQUEST +
                    "\nResponse: {\"msg\": \"Invalid Command.\"}",
                    HttpStatus.BAD_REQUEST);
        }

    }

}
