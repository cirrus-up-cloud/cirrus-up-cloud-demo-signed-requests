# README #

### What is this repository for? ###

* Demo Spring Boot application that shows how to digitally `sign` HTTP requests as an authentication mechanism.

### How to set up? ###

* Build with maven
mvn package

* Run the jar in **dev** mode
java  -Dspring.profiles.active=dev  -jar target/cirrus-up-cloud-demo-signed-requests-1.0-SNAPSHOT.jar


### Making Requests ###
* The piece of code presented below can be used to create a HTTP request to the server

```java
    private void run() throws IOException, NoSuchAlgorithmException {
        Date currentTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        Key key = new SecretKeySpec("YNM8qQYCBEVwYMcdqn78xVwTV8yZ9TdC".getBytes(), "hmac-sha256");

        HttpGet httpget = new HttpGet("http://localhost:8080/hello");
        System.out.println("Executing request " + httpget.getRequestLine());

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Date", sdf.format(currentTime));
        headers.put("Content-Type", "application/json");
        headers.put("content-md5", calculateMD5(""));

        List<String> headersList = Lists.newArrayList();
        headersList.add("Date");
        headersList.add("Content-Type");
        headersList.add("content-md5");

        Signature signature = new Signature("John Doe", "hmac-sha256", null, headersList);
        Signer signer = new Signer(key, signature);
        Signature get = signer.sign(httpget.getMethod(), httpget.getURI().toString(), headers);

        CloseableHttpClient client = HttpClientBuilder.create().build();
        httpget.setHeader("Authorization", get.toString());
        httpget.setHeader("Date", sdf.format(currentTime));
        httpget.setHeader("Content-Type", "application/json");
        httpget.setHeader("content-md5", calculateMD5(""));

        CloseableHttpResponse execute = client.execute(httpget);
        System.out.println("Response: " + execute.getStatusLine().getStatusCode() + " Content: " + EntityUtils.toString(execute.getEntity()));
    }

    private String calculateMD5(String contentToEncode) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(contentToEncode.getBytes());
        return new String(Base64.encodeBase64(digest.digest()));
    }
```