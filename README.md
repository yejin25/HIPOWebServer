# HIPOWebServer
‘HIPO’는 ‘High’와 ‘Potential’의 합성어에서 유래된 말로 출세 가도에 있는 사람, 승진이 빠른 사람을 의미하며 팀원 모두 이번 과제를 높은 잠재력을 활용하여 열정적으로 수행하고 팀 전체의 발전을 최종 목표로 하는 포부를 담아 팀명을 위와 같이 정하였다.

<hr/>   

## **🚘옥내주차장 차량 위치 확인 시스템 : casition🚖** 을 위한 REST API

Springboot를 사용   
HashMap을 사용하여 구현

[📑데이터 명세]
```
id :
    pw:
    userName:
    userPhoneNumber:
    <<userCarNumber>>
    checkCarLicense:
    presentCarList {
        // 현재 기록

        {
            "time": "STRING",
            "parkingTime": "STRING",        //parking 됐을 때 업데이트
            "outTime": "STRING"             //output (출차) 됐을 때 업데이트
            "isBalanceCheck": "STRING"      //blanceup (정산) 됐을 때 업데이트
	        "parkingArea": "STRING"     //parking 됐을 때 업데이트
        }

    }
    allCarList {
        // 전체 기록
         "parkingArea": "STRING"
         "time": "STRING",
         "outTime": "STRING"
    }



1. Raspberry Pi
post
- /pi/{deviceType}

{deviceType} = STRING -> input, output, parking //입차, 출차, 주차 (카메라)

if ok 200, no 400 //200 = success, 400 = Bad Request

{
    "time": "STRING",
    "plateNumber": "STRING"
}if {deviceType} = input

{
    "time": "STRING",
    "plateNumber": "STRING",
    "parkingArea": "STRING"
}if {deviceType} = output

{
    "time": "STRING",
    "plateNumber": "STRING",
    "parkingArea": "STRING"
}if {deviceType} = parking

2. Android
post
- /android/user/signup  //회원가입

{
    "id": "STRING",
    "pw": "STRING",
    "userName": "STRING",
    "userphoneNumber": "STRING"
} if ok 200, no 400

- /android/user/{id}/registercar    //차량번호등록 - 차량등록증

{id} = STRING -> id

{
    "carNumber": "STRING",
    "file": "IMAGE / metatype"
} if ok 200, no 400

- /android/user/signin  //로그인

{
    "id": "STRING",
    "pw": "STRING"
} -> if no, return 404

//returnal{
    "userName": "STRING",
} if ok 200

//returnal{
    "userName": "STRING",
    "checkCarLicense": "STRING"
} if ok 200, (checkCarLicense != NULL)

- /android/user/find/{typical}  // id, pw 찾기

{typical} = STRING -> id, pw

if id 
{
    "userName": "STRING",
    "userPhoneNumber": "STRING"
}

//returnal
{
    "userId": "STRING" -> if no, return null 
}

if pw
{
    "userId": "STRING",
    "userPhoneNumber": "STRING"
}

//returnal
{
    "isChecked": "STRING" -> OK, NO 
}

- /android/admin/signin

{
}if no 404, ok 200

-----------------------------------------------------------------
get

- /android/user/myinfo/{id}     //사용자 정보 - 프로필, 정산

{id} = STRING -> id

//returnal
{
    "LIST": {
        "carNumber": "STRING",
        "inputDate": "STRING_DATE",
        "userBalance": "STRING"
    }    
}

- /android/user/myinfo/all/{id}     //모든 이용기록

{id} = STRING -> id

//returnal
{
    "LIST": {
        "carNumber": "STRING",
        "inputDate": "STRING_DATE",
        "userBalance": "STRING"     //정산 금액
    }    
}

- /android/user/myinfo/balanceup/{id}     //정산 시 상태 변경

{id} = STRING -> id

//returnal
{
} if isBalanceCheck = T, 200 // 정산완료

- /android/user/parkingArea/{id}

{id} = STRING

{
    "parkingArear": "STRING" -> byte[]
}

-/android/admin/accepted/{id}   //자동차등록증과 차량 번호 일치 확인

{id} = STRING -> id

//returnal
{
} if checkCarLicense = T, 200 // 승인

- /android/admin/licenseimages 

//returnal
{
}if no 400, ok 200,  "id": "STRING" -> byte[]

- /android/user/parkingArea/{id}

{id} = STRING -> id 

//returnal
{
}if no 400, ok 200, "parkingArea": "STRING" -> byte[]
```



[REST API 중 자동차 등록증 등록 관련 시나리오📜]
![image](https://user-images.githubusercontent.com/40768187/125300809-54de7800-e365-11eb-8998-bfcb322bad97.png)   











## 🚘옥내주차장 차량 위치 확인 시스템🚖

주차장 내에서 차량의 위치를 잊어버리거나, 기억하기 귀찮을 때 어플로 이를 확일할 수 있으면 어떨까라는 생각에서 나온 아이디어

![image](https://user-images.githubusercontent.com/40768187/125300809-54de7800-e365-11eb-8998-bfcb322bad97.png)   


주차장 천장에 달린 카메라와 OpenCV 라이브러리를 사용하여 번호판을 인식하여 문자열을 추출하고 이를 서버로 전송한다.   

어플리케이션에서는 차량 번호와 본인 차량임을 인증하기 위한 자동차 등록증을 등록하고, 관리자에 의해 등록이 승인되면 서비스를 이용할 수 있게 된다.

사용자가 등록한 번호와 추출된 문자열이 동일한 경우 어플리케이션의 메인 페이지에서 주차 구역에 대한 정보를 보여준다.



