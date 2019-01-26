package com.wps.proxy.portlet.business;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.wps.proxy.portlet.ProxyPortlet;

import org.glassfish.jersey.SslConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServiceClientHTTPS {

    final private static Logger logger = LoggerFactory.getLogger(WebServiceClientHTTPS.class);
    private static WebServiceClientHTTPS instance;
    private Gson gson;
    private Class<?> classe;

    public static final String PATH_KEYSTORE = "PathKeystoreWlscl";
    public static final String PASSWORD_KEYSTORE = "PasswordKeystore";
    public static final String KEYSTORE_TYPE = "pkcs12";
    private static final int CONNECTION_TIMEOUT = 90000;

    /**
     * Singleton de la clase WebServiceClientHTTP
     * 
     * @return WebServiceClientHTTP
     */
    public static WebServiceClientHTTPS getInstance(Class<?> classe) {
        instance = new WebServiceClientHTTPS(classe);
        return instance;
    }

    public WebServiceClientHTTPS(Class<?> classe) {
        gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDateDeserializer())
                .registerTypeAdapter(Date.class, new JsonDateSerializer())
                .registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter()).create();
        this.classe = classe;
    }

    public Object procesarRequest(String endpoint, Object request) {
        OutputStream os = null;
        HttpsURLConnection conn = null;
        BufferedReader br = null;
        try {
            String dataRequest = gson.toJson(request);
            URL url = new URL(endpoint);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(getSocketFactory());
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(90000);

            os = conn.getOutputStream();
            os.write(dataRequest.getBytes("UTF-8"));
            os.flush();
            StringBuilder response = new StringBuilder();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                logger.info("RESPONSE CODE: " + conn.getResponseCode());
                br = new BufferedReader(new InputStreamReader((conn.getInputStream()), "UTF-8"));
                String output = null;
                while ((output = br.readLine()) != null) {
                    response.append(output);
                }
                logger.info("RESPONSE: " + response.toString());
            } else {
                logger.info("ERROR HTTP CLIENT: " + conn.getResponseCode());
                logger.info("ERROR HTTP CLIENT Mensaje: " + conn.getResponseMessage());
                return null;
            }
            Object data = transform(response.toString());
            return data;
        } catch (MalformedURLException e) {
            logger.error("MalformedURLException", e);
            return null;
        } catch (IOException e) {
            logger.error("IOException", e);
            return null;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }

                if (os != null) {
                    os.close();
                }

                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e2) {
                logger.error("Exception", e2);
            }
        }

    }

    public Object transform(String data) {
        try {
            return gson.fromJson(data, classe);
        } catch (Exception e) {
            logger.error("Exception", e);
            return null;
        }

    }

    public static SSLSocketFactory getSocketFactory() {
        // String pathKeystore = System.getenv(PATH_KEYSTORE);
        // String passKeystore = System.getenv(PASSWORD_KEYSTORE);

        String pathKeystore = "F:\\datos\\PortalPagos\\keystore\\wlscl\\wpsmed30.p12";
        String passKeystore = "pragma";

        SslConfigurator sslConfig = SslConfigurator.newInstance();
        if (pathKeystore != null && !pathKeystore.isEmpty() && !pathKeystore.equals("null") && passKeystore != null
                && !passKeystore.isEmpty() && !passKeystore.equals("null")) {
            sslConfig = SslConfigurator.newInstance().keyStoreFile(pathKeystore).keyStoreType(KEYSTORE_TYPE).keyPassword(passKeystore);
            KeyStore keyStore;
            try {
                String keystoreFilename = pathKeystore;
                FileInputStream fIn = new FileInputStream(keystoreFilename);
                keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
                char[] password = passKeystore.toCharArray();

                keyStore.load(fIn, password);
                sslConfig.keyStore(keyStore);
            } catch (Exception e) {
                logger.error("ERROR al cargar la keyStore para SSL");
            }
        } else {
            logger.info("ERROR: No es posible obtener la variable de entorno PATH_KEYSTORE o PASSWORD_KEYSTORE");
        }

        SSLContext ctx = sslConfig.createSSLContext();
        SSLSocketFactory sslFactory = ctx.getSocketFactory();
        return sslFactory;
    }

}

class JsonDateDeserializer implements JsonDeserializer<Date> {
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        long time = json.getAsJsonPrimitive().getAsLong();
        Date d = new Date(time);
        return d;
    }
}

class JsonDateSerializer implements JsonSerializer<Date> {
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        return src == null ? null : new JsonPrimitive(src.getTime());
    }
}

class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
    public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        // return Base64.getDecoder().decode(json.getAsString());
        return DatatypeConverter.parseBase64Binary(json.getAsString());
    }

    public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
        // return new JsonPrimitive(Base64.getEncoder().encodeToString(src));
        return new JsonPrimitive(DatatypeConverter.printBase64Binary(src));
    }
}