package kr.ac.jbnu.se.hipowebserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class Controller {
    public static HashMap<String, ArrayList<Map<String, Object>>> dataHashMap = new HashMap<String, ArrayList<Map<String, Object>>>(); // db 대용

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

    /*
        Raspberry Pi
    */
    @RequestMapping(value = "/pi/{deviceType}", method = RequestMethod.POST, produces = "application/json") // pi - post
    @ResponseBody
    public ResponseEntity<?> piPostCarDataEntity(HttpServletRequest request, @PathVariable String deviceType, @RequestBody Map<String, Object> requestMap) {
        ResponseEntity<?> responseEntity = null;

        if (requestMap.get("time") != null && requestMap.get("plateNumber") != null) { // 시간 및 자동차 번호 확인
            for (String id : dataHashMap.keySet()) {
                ArrayList<Map<String, Object>> uArray = dataHashMap.get(id);
                Map<String, Object> uMap = uArray.get(0);
                boolean isStatusOK = false;

                if (uMap.containsKey("userCarNumber")) {
                    if (uMap.get("userCarNumber").toString().equals(requestMap.get("plateNumber").toString())) { // 차 번호판이 겹치냐?
                        ArrayList<Map<String, Object>> presentCarList = null;
                        ArrayList<Map<String, Object>> allCarList = null;
                        Map<String, Object> dataMap = null;
                        Map<String, Object> allDataMap = null;

                        if (uMap.get("presentCarList") != null) {
                            presentCarList = (ArrayList<Map<String, Object>>) uMap.get("presentCarList");
                            dataMap = presentCarList.get(0);

                        } else {
                            presentCarList = new ArrayList<Map<String, Object>>();
                            dataMap = new HashMap<String, Object>();
                        }

                        if (uMap.get("allCarList") != null) {
                            allCarList = (ArrayList<Map<String, Object>>) uMap.get("allCarList");

                        } else {
                            allCarList = new ArrayList<Map<String, Object>>();
                        }

                        allDataMap = new HashMap<String, Object>();

                        switch (deviceType) {
                            case "input":
                                dataMap.put("time", requestMap.get("time"));
                                dataMap.put("isBalanceCheck", "F");
                                isStatusOK = true;
                                break;

                            case "output":
                                if (dataMap.containsKey("time") && dataMap.containsKey("parkingArea")) {
                                    if (dataMap.get("isBalanceCheck").toString().equals("T")) {
                                        allDataMap.put("time", dataMap.get("time"));
                                        allDataMap.put("outTime", requestMap.get("time"));
                                        allDataMap.put("parkingArea", requestMap.get("parkingArea"));
                                        isStatusOK = true;
                                    }
                                }
                                break;

                            case "parking":
                                dataMap.put("parkingTime", requestMap.get("time"));
                                dataMap.put("parkingArea",requestMap.get("parkingArea"));
                                isStatusOK = true;
                                break;
                        }

                        if (isStatusOK) {
                            presentCarList.clear();
                            presentCarList.add(0, dataMap);
                            if (deviceType.equals("output")) {
                                uMap.remove("presentCarList");
                                allCarList.add(allDataMap);
                                uMap.put("allCarList", allCarList);

                            } else {
                                uMap.remove("presentCarList");
                                uMap.put("presentCarList", presentCarList);
                            }
                            uArray.clear();
                            uArray.add(0, uMap);
                            dataHashMap.remove(id);
                            dataHashMap.put(id, uArray);

                            responseEntity = new ResponseEntity<>(dataMap, HttpStatus.OK);

                        } else {
                            responseEntity = new ResponseEntity<>("NO_BALANCED", HttpStatus.NOT_FOUND);
                        }

                    }

                } else {
                    responseEntity= new ResponseEntity<>("NOT_FOUND", HttpStatus.NOT_FOUND);
                }
            }

            if (responseEntity == null) {
                responseEntity = new ResponseEntity<>("NOT_FOUND", HttpStatus.NOT_FOUND);
            }

        } else {
            responseEntity = new ResponseEntity<>("NOT_CONTAIN", HttpStatus.BAD_REQUEST);
        }

        makeStatusLog(request, responseEntity);
        return responseEntity;
    }

    /*
        Android
    */
    @RequestMapping(value = "/android/user/signup", method = RequestMethod.POST, produces = "application/json") // login - sign up
    @ResponseBody
    public ResponseEntity<?> androidPostUserSignUpEntity(HttpServletRequest request, @RequestBody Map<String, Object> requestMap) {
        ResponseEntity<?> responseEntity = null;
        ArrayList<Map<String, Object>> receiveUserData;

        if (!dataHashMap.containsKey(requestMap.get("id"))) {
            if (requestMap.get("pw") != null && requestMap.get("userName") != null
                    && requestMap.get("userEmail") != null) {
                receiveUserData = new ArrayList<Map<String, Object>>();

                Map<String, Object> tempMap = requestMap;
                receiveUserData.add(requestMap);
                dataHashMap.put(requestMap.get("id").toString(), receiveUserData);

                responseEntity = new ResponseEntity<>(tempMap, HttpStatus.OK);

            } else {
                responseEntity = new ResponseEntity<>("DATA_NOT_RECOGNIZE", HttpStatus.BAD_REQUEST);
            }

        } else {
            responseEntity = new ResponseEntity<>("IS_HAVE_KEY", HttpStatus.BAD_REQUEST);
        }

        makeStatusLog(request, responseEntity);
        return responseEntity;
    }

    @RequestMapping(value = "/android/user/{id}/changepw", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> androidPostChangeUserPasswordEntity(HttpServletRequest request, @PathVariable String id, @RequestBody Map<String, Object> requestMap) {
        ResponseEntity<?> responseEntity = null;

        if (id != null && requestMap != null) {
            if (dataHashMap.containsKey(id)) {
                ArrayList<Map<String, Object>> userList = dataHashMap.get(id);
                Map<String, Object> userMap = userList.get(0);

                userMap.put("pw", requestMap.get("changedPW"));

                userList.clear();
                userList.add(userMap);
                dataHashMap.remove(id);
                dataHashMap.put(id, userList);

                responseEntity = new ResponseEntity<>(userMap, HttpStatus.OK);

            } else {
                responseEntity = new ResponseEntity<>("IS_NOT_HAVE_KEY", HttpStatus.BAD_REQUEST);
            }

        } else {
            responseEntity = new ResponseEntity<>("DATA_NOT_RECOGNIZE", HttpStatus.BAD_REQUEST);
        }
        return responseEntity;
    }

    @RequestMapping(value = "/android/user/{id}/registercar", method = RequestMethod.POST, produces = "application/json", headers = ("content-type=multipart/*"), consumes = "multipart/form-data") // car - register car
    @ResponseBody
    public ResponseEntity<?> androidPostUserCarRegisterEntity(HttpServletRequest request, @PathVariable String id, @RequestParam("file") MultipartFile file, @RequestParam("carNumber") String carNumber) {
        ResponseEntity<?> responseEntity = null;

        if (id != null && carNumber != null) {
            if (dataHashMap.containsKey(id)) {
                ArrayList<Map<String, Object>> receiveData = dataHashMap.get(id);
                Map<String, Object> receiveMap = receiveData.get(0);

                receiveMap.put("unAcceptCarNumber", carNumber);
                receiveMap.put("checkCarLicense", "F");

                String reName = id;


                String savePath = FileSystemView.getFileSystemView().getHomeDirectory().toString() + "/hipo_img/" + reName;

                File saveFile = new File(savePath);
                try {
                    file.transferTo(saveFile);// file save
                } catch (IOException e) {
                    e.printStackTrace();
                }


                receiveData.clear();
                receiveData.add(receiveMap);
                dataHashMap.remove(id);
                dataHashMap.put(id, receiveData);

                responseEntity = new ResponseEntity<>(receiveMap, HttpStatus.OK);
            } else {
                responseEntity = new ResponseEntity<>("IS_NOT_HAVE_KEY", HttpStatus.BAD_REQUEST);
            }
        }
         else {
            responseEntity = new ResponseEntity<>("DATA_NOT_RECOGNIZE", HttpStatus.BAD_REQUEST);
        }

        return responseEntity;
    }

    @RequestMapping(value = "/android/user/signin", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> androidGetUserSignInEntity(HttpServletRequest request, @RequestBody Map<String, Object> requestMap) {
        ResponseEntity<?> responseEntity = null;
        Map<String, Object> returnalMap = new HashMap<String, Object>();

        if (requestMap.get("id") != null && requestMap.get("pw") != null) {
            if (dataHashMap.containsKey(requestMap.get("id"))) {
                ArrayList<Map<String, Object>> userArray = dataHashMap.get(requestMap.get("id"));
                Map<String, Object> userMap = userArray.get(0);

                if (userMap.get("pw").toString().equals(requestMap.get("pw").toString())) {
                        returnalMap.put("userName", userMap.get("userName"));

                    if (userMap.get("checkCarLicense") != null){
                        Map<String, Object> checkCarLicenseMap = new HashMap<>();
                        returnalMap.put("checkCarLicense", userMap.get("checkCarLicense"));
                    }

                    if(userMap.get("userCarNumber") != null){
                        returnalMap.put("userCarNumber", userMap.get("userCarNumber"));
                    }

                    responseEntity = new ResponseEntity<>(returnalMap, HttpStatus.OK);

                } else {
                    responseEntity = new ResponseEntity<>("NO", HttpStatus.NOT_FOUND);
                }

            } else {
                responseEntity = new ResponseEntity<>("NO", HttpStatus.NOT_FOUND);
            }

        } else {
            responseEntity = new ResponseEntity<>("DATA_NOT_RECOGNIZE", HttpStatus.BAD_REQUEST);
        }

        makeStatusLog(request, responseEntity);
        return responseEntity;
    }

    @RequestMapping(value = "/android/user/find/id", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> AndroidPostFindUserID(HttpServletRequest request, @RequestBody Map<String, Object> requestMap) {
        ResponseEntity<?> responseEntity = null;

        if (requestMap.get("userName") != null && requestMap.get("userEmail") != null
                && !requestMap.get("userName").equals("") && !requestMap.get("userEmail").equals("")) {

            if (!dataHashMap.isEmpty()) {
                for (String key : dataHashMap.keySet()) {
                    Map<String, Object> userMap = dataHashMap.get(key).get(0);

                    if (userMap.get("userName").equals(requestMap.get("userName"))
                            && userMap.get("userEmail").equals(requestMap.get("userEmail"))) {

                        MailController mailController = MailController.getInstance();
                        mailController.sendIDMail(key, userMap.get("userEmail").toString(), userMap.get("userName").toString());

                        responseEntity = new ResponseEntity<>("SEND_SUCCESS", HttpStatus.OK);
                        break;

                    } else {
                        responseEntity = new ResponseEntity<>("DONT_HAVE", HttpStatus.NOT_FOUND);
                    }
                }
            }
        } else {
            responseEntity = new ResponseEntity<>("DATA_IS_NULL", HttpStatus.BAD_REQUEST);
        }


        makeStatusLog(request, responseEntity);
        return responseEntity;
    }

    @RequestMapping(value = "/android/user/find/pw", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> AndroidPostFindUserPW(HttpServletRequest request, @RequestBody Map<String, Object> requestMap) {
        ResponseEntity<?> responseEntity = null;

        if (requestMap.get("userName") != null && requestMap.get("userEmail") != null && requestMap.get("id") != null
                && !requestMap.get("userName").equals("") && !requestMap.get("userEmail").equals("") && !requestMap.get("id").equals("")) {

            if (!dataHashMap.isEmpty()) {
                for (String key : dataHashMap.keySet()) {
                    Map<String, Object> userMap = dataHashMap.get(key).get(0);

                    if (userMap.get("userName").equals(requestMap.get("userName"))
                            && userMap.get("userEmail").equals(requestMap.get("userEmail"))
                                && userMap.get("id").equals(requestMap.get("id"))) {

                        MailController mailController = MailController.getInstance();
                        mailController.sendPWMail(userMap.get("pw").toString(), userMap.get("userEmail").toString(), userMap.get("userName").toString());

                        responseEntity = new ResponseEntity<>("SEND_SUCCESS", HttpStatus.OK);
                        break;

                    } else {
                        responseEntity = new ResponseEntity<>("DONT_HAVE", HttpStatus.NOT_FOUND);
                    }
                }
            }
        } else {
            responseEntity = new ResponseEntity<>("DATA_IS_NULL", HttpStatus.BAD_REQUEST);
        }


        makeStatusLog(request, responseEntity);
        return responseEntity;
    }

    @RequestMapping(value = "/android/admin/signin", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> androiPostAdminSignInEntity(HttpServletRequest request, @RequestBody Map<String, Object> requestMap) {
        ResponseEntity<?> responseEntity = null;
        String password = "qlalfqjsghqwer1234";

        if (requestMap.get("pw") != null) {
                if (password.equals(requestMap.get("pw").toString())) {
                    responseEntity = new ResponseEntity<>("OK", HttpStatus.OK);

                } else {
                    responseEntity = new ResponseEntity<>("NO", HttpStatus.NOT_FOUND);
                }
        }else{
            responseEntity = new ResponseEntity<>("NO_DATA_RECOGNIZE",HttpStatus.BAD_REQUEST);
        }
        makeStatusLog(request, responseEntity);
        return responseEntity;
    }

    @RequestMapping(value = "/android/admin/accepted/{id}" , method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> AndroidPostCheckCarLicense(HttpServletRequest request, @PathVariable String id) {
        ResponseEntity<?> responseEntity = null;

        if(dataHashMap.containsKey(id)){
            ArrayList<Map<String, Object>> userList = dataHashMap.get(id);
            Map<String, Object> userMap = userList.get(0);

            userMap.put("userCarNumber", userMap.get("unAcceptCarNumber").toString());
            userMap.put("checkCarLicense", "T");

            userMap.remove("unAcceptCarNumber");

            userList.clear();
            userList.add(userMap);
            dataHashMap.remove(id);
            dataHashMap.put(id, userList);

            responseEntity = new ResponseEntity<>(userMap, HttpStatus.OK);
        }
        else {
            responseEntity = new ResponseEntity<>("IS_NOT_HAVE_KEY", HttpStatus.BAD_REQUEST);
        }

        makeStatusLog(request, responseEntity);
        return responseEntity;
    }

    @RequestMapping(value = "/android/user/parkingArea/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> AndroidGetParkingArea(HttpServletRequest request, @PathVariable String id) {
        ResponseEntity<?> responseEntity = null;

        HashMap<String, Object> userDataHashMap = new HashMap<String, Object>();

        if(dataHashMap.containsKey(id)){
            ArrayList<Map<String, Object>> userList = dataHashMap.get(id);
            Map<String, Object> userMap = userList.get(0);

            if (userMap.containsKey("presentCarList")) {

                ArrayList<Map<String, Object>> userPresentList = (ArrayList<Map<String, Object>>) userMap.get("presentCarList");
                Map<String, Object> userPresentMap = userPresentList.get(0);

                if(userPresentMap.containsKey("parkingArea")) {
                    String filePath = FileSystemView.getFileSystemView().getHomeDirectory().toString() + "/parkinglot_img/";

                    try {
                        userDataHashMap.put(userPresentMap.get("parkingArea").toString(), Files.readAllBytes(new File(filePath + userPresentMap.get("parkingArea").toString()).toPath()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    responseEntity = new ResponseEntity<>(userDataHashMap, HttpStatus.OK);
                }else{
                    responseEntity = new ResponseEntity<>("NO_DATA_ABOUT_AREA", HttpStatus.BAD_REQUEST);
                }
            }
            else{
                responseEntity = new ResponseEntity<>("NO_DATA", HttpStatus.BAD_REQUEST);
            }
        }else{
            responseEntity = new ResponseEntity<>("NOT_HAVE_KEY", HttpStatus.BAD_REQUEST);
        }

        makeStatusLog(request, responseEntity);
        return responseEntity;

    }


    @RequestMapping(value = "/android/user/myinfo/balanceup/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> AndroidGetBalanceUpCar(HttpServletRequest request, @PathVariable String id) {
        ResponseEntity<?> responseEntity = null;

        if (dataHashMap.containsKey(id)) {
            ArrayList<Map<String, Object>> userList = dataHashMap.get(id);
            Map<String, Object> userMap = userList.get(0);

            if (userMap.containsKey("presentCarList")) {
                ArrayList<Map<String, Object>> userPresentList = (ArrayList<Map<String, Object>>) userMap.get("presentCarList");
                Map<String, Object> userPresentMap = userPresentList.get(0);

                userPresentMap.put("isBalanceCheck", "T");

                userPresentList.clear();
                userPresentList.add(userPresentMap);
                userMap.remove("presentCarList");
                userMap.put("presentCarList", userPresentList);
                userList.clear();
                userList.add(userMap);
                dataHashMap.remove(id);
                dataHashMap.put(id, userList);

                responseEntity = new ResponseEntity<>(userPresentMap, HttpStatus.OK);

            } else {
                responseEntity = new ResponseEntity<>("NOT_CONTAIN", HttpStatus.NOT_FOUND);
            }

        } else {
            responseEntity = new ResponseEntity<>("NOT_CONTAIN", HttpStatus.NOT_FOUND);
        }

        makeStatusLog(request, responseEntity);
        return responseEntity;
    }

    @RequestMapping(value = "/android/user/myinfo/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> AndroidGetCarUserInfo(HttpServletRequest request, @PathVariable String id) {
        ResponseEntity<?> responseEntity = null;

        if (dataHashMap.containsKey(id)) {
            ArrayList<Map<String, Object>> userList = dataHashMap.get(id);
            Map<String, Object> userMap = userList.get(0);

            if (userMap.containsKey("presentCarList")) {
                responseEntity = new ResponseEntity<>(userMap.get("presentCarList"), HttpStatus.OK);

            } else {
                responseEntity = new ResponseEntity<>("IS_NOT", HttpStatus.BAD_REQUEST);
            }

        } else {
            responseEntity = new ResponseEntity<>("IS_NOT_HAVE_KEY", HttpStatus.BAD_REQUEST);
        }

        makeStatusLog(request, responseEntity);
        return responseEntity;
    }

    @RequestMapping(value = "/android/user/myinfo/all/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> AndroidGetAllCarUserInfo(HttpServletRequest request, @PathVariable String id) {
        ResponseEntity<?> responseEntity = null;

        if (dataHashMap.containsKey(id)) {
            ArrayList<Map<String, Object>> userList = dataHashMap.get(id);
            Map<String, Object> userMap = userList.get(0);

            if (userMap.containsKey("allCarList")) {
                responseEntity = new ResponseEntity<>(userMap.get("allCarList"), HttpStatus.OK);

            } else {
                responseEntity = new ResponseEntity<>("IS_NOT", HttpStatus.BAD_REQUEST);
            }

        } else {
            responseEntity = new ResponseEntity<>("IS_NOT_HAVE_KEY", HttpStatus.BAD_REQUEST);
        }

        makeStatusLog(request, responseEntity);
        return responseEntity;
    }

    @RequestMapping(value = "/android/admin/licenseimages", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> androidGetLicenseImages(HttpServletRequest request) {
        ResponseEntity<?> responseEntity = null;
        HashMap<String, Object> userDataHashMap = new HashMap<String, Object>();

        String filePath = FileSystemView.getFileSystemView().getHomeDirectory().toString() + "/hipo_img/";

        if (!dataHashMap.isEmpty()) {
            for (String key : dataHashMap.keySet()) {
                if (dataHashMap.get(key).get(0).containsKey("checkCarLicense") && dataHashMap.get(key).get(0).get("checkCarLicense").equals("F")) {
                    try {
                        userDataHashMap.put(key, Files.readAllBytes(new File(filePath + key).toPath()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
            if (!userDataHashMap.isEmpty()) {
                responseEntity = new ResponseEntity<>(userDataHashMap, HttpStatus.OK);
            } else {
                responseEntity = new ResponseEntity<>("EMPTY_CARLICENSE_IMAGE", HttpStatus.NOT_FOUND);
            }

        } else {
            responseEntity = new ResponseEntity<>("EMPTY_USER_DATA", HttpStatus.NOT_FOUND);
        }

        makeStatusLog(request, responseEntity);
        return responseEntity;
    }

    /*
        Debug
    */
    public void makeStatusLog(HttpServletRequest req, ResponseEntity<?> resEntity) { // 디버그용
        if (resEntity.getStatusCode() == HttpStatus.OK) {
            log.info("- Request Addr : " + req.getRemoteAddr() + " Response Entity : " + resEntity.getStatusCode() + " Response Body : " + resEntity.getBody() + " -");

        } else {
            log.warn("- Request Addr : " + req.getRemoteAddr() + " Response Entity : " + resEntity.getStatusCode() + " Response Body : " + resEntity.getBody() + " -");
        }
    }

}
