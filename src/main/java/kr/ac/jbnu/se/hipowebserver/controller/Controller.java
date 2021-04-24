package kr.ac.jbnu.se.hipowebserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class Controller {
    public static HashMap<String, ArrayList<Map<String, Object>>> dataHashMap = new HashMap<String, ArrayList<Map<String, Object>>>(); // db 대용 테스트 - device string + arrayList [ Key value ]

    @RequestMapping(value = "/", method = RequestMethod.GET) // get all data
    @ResponseBody
    public ResponseEntity<?> getResponseEntity(HttpServletRequest request) {
        ResponseEntity<?> responseEntity = null;

        if (dataHashMap.size() != 0) {
            responseEntity = new ResponseEntity<>(dataHashMap, HttpStatus.OK);

        } else {
            responseEntity = new ResponseEntity<>("NO_DATA", HttpStatus.NOT_FOUND);
        }

        makeStatusLog(request, responseEntity);
        return responseEntity;
    }

    @RequestMapping(value = "/pi/{deviceType}", method = RequestMethod.POST, produces = "application/json") // post pi data
    @ResponseBody
    public ResponseEntity<?> postResponseEntity(HttpServletRequest request, @PathVariable String deviceType, @RequestBody Map<String, Object> requestMap) {
        ResponseEntity<?> responseEntity = null;

        if (requestMap.get("time") != null && requestMap.get("plateNumber") != null) { // 시간 및 자동차 번호 확인
            for (String id : dataHashMap.keySet()) {
                ArrayList<Map<String, Object>> uArray = dataHashMap.get(id);
                Map<String, Object> uMap = uArray.get(0);

                if (uMap.containsKey("userCarNumber")) {
                    if (uMap.get("userCarNumber").toString().equals(requestMap.get("plateNumber").toString())) { // 차 번호판이 겹치냐?
                        ArrayList<Map<String, Object>> presentCarList = null;
                        Map<String, Object> dataMap = null;

                        if (uMap.get("presentCarList") != null) {
                            presentCarList = (ArrayList<Map<String, Object>>) uMap.get("presentCarList");
                            dataMap = presentCarList.get(0);

                        } else {
                            presentCarList = new ArrayList<Map<String, Object>>();
                            dataMap = new HashMap<String, Object>();
                        }

                        switch (deviceType) {
                            case "input":
                                dataMap.put("time", requestMap.get("time"));
                                break;

                            case "output":
                                dataMap.put("outTime", requestMap.get("time"));
                                break;

                            case "parking":
                                dataMap.put("parkingTime", requestMap.get("time"));
                                break;
                        }

                        presentCarList.clear();
                        presentCarList.add(0, dataMap);
                        uMap.remove("presentCarList");
                        uMap.put("presentCarList", presentCarList);
                        uArray.clear();
                        uArray.add(0, uMap);
                        dataHashMap.remove(id);
                        dataHashMap.put(id, uArray);

                        responseEntity = new ResponseEntity<>(dataMap, HttpStatus.OK);
                    }

                } else {
                    responseEntity= new ResponseEntity<>("NOT_FOUND", HttpStatus.NOT_FOUND);
                }
            }

        } else {
            responseEntity = new ResponseEntity<>("NOT_CONTAIN", HttpStatus.BAD_REQUEST);
        }

        makeStatusLog(request, responseEntity);
        return responseEntity;
    }

    @RequestMapping(value = "/android/user/signup", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> androidPostUserSignUpEntity(HttpServletRequest request, @RequestBody Map<String, Object> requestMap) {
        ResponseEntity<?> responseEntity = null;
        ArrayList<Map<String, Object>> receiveUserData;

        if (requestMap.get("id") != null && requestMap.get("pw") != null && requestMap.get("userName") != null && requestMap.get("userPhoneNumber") != null) {
            if (!dataHashMap.containsKey(requestMap.get("id"))) {
                receiveUserData = new ArrayList<Map<String, Object>>();

                Map<String, Object> tempMap = requestMap;
                receiveUserData.add(requestMap);
                dataHashMap.put(requestMap.get("id").toString(), receiveUserData);

                responseEntity = new ResponseEntity<>(tempMap, HttpStatus.OK);

            } else {
                responseEntity = new ResponseEntity<>("IS_HAVE_KEY", HttpStatus.BAD_REQUEST);
            }


        } else {
            responseEntity = new ResponseEntity<>("DATA_NOT_RECOGNIZE", HttpStatus.BAD_REQUEST);
        }

        makeStatusLog(request, responseEntity);
        return responseEntity;
    }

    public void makeStatusLog(HttpServletRequest req, ResponseEntity<?> resEntity) { // 디버그용
        if (resEntity.getStatusCode() == HttpStatus.OK) {
            log.info("- Request Addr : " + req.getRemoteAddr() + " Response Entity : " + resEntity.getStatusCode() + " Response Body : " + resEntity.getBody() + " -");

        } else {
            log.warn("- Request Addr : " + req.getRemoteAddr() + " Response Entity : " + resEntity.getStatusCode() + " Response Body : " + resEntity.getBody() + " -");
        }
    }

}
