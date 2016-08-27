package tw.com.mitake.sms;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tw.com.mitake.sms.result.MitakeSmsQueryAccountPointResult;
import tw.com.mitake.sms.result.MitakeSmsQueryMessageStatusResult;
import tw.com.mitake.sms.result.MitakeSmsResult;
import tw.com.mitake.sms.result.MitakeSmsSendResult;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MitakeSmsSender {
    private static final Logger LOG = LoggerFactory.getLogger(MitakeSmsSender.class);
    private static final String BASE_URL = "http://smexpress.mitake.com.tw";
    private static final String SEND_URL = BASE_URL + "/SmSendGet.asp";
    private static final String QUERY_URL = BASE_URL + "/SmQueryGet.asp";

    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_MSG_ID = "msgid";
    private static final String KEY_MESSAGE = "smbody";
    private static final String KEY_DESTINATION = "dstaddr";
    private static final String KEY_ENCODING = "encoding";
    private static final String KEY_SCHEDULE = "dlvtime";
    private static final String KEY_TIME_TO_LIVE = "vldtime";
    private static final String KEY_RESPONSE = "response";
    private static final String KEY_DEST_NAME = "DestName";
    private static final String KEY_CLIENT_ID = "ClientID";

    public MitakeSmsSendResult send(SendOptions opts) {
        HttpURLConnection conn = null;

        try {
            URL url = buildUrl(opts);

            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                return (MitakeSmsSendResult) connectionError(ConnectionResult.FAIL);
            }

            ArrayList<String> response = retrieveResponse(conn);

            return new MitakeSmsSendResult(response, opts.getDestinations().get(0));
        } catch (Exception e) {
            LOG.error(e.getMessage());

            return (MitakeSmsSendResult) connectionError(ConnectionResult.EXCEPTION);
        } finally {
            closeConnection(conn);
        }
    }

    public MitakeSmsQueryAccountPointResult queryAccountPoint() {
        HttpURLConnection conn = null;

        try {
            URL url = buildQueryAccountPointUrl();

            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                return (MitakeSmsQueryAccountPointResult) connectionError(ConnectionResult.FAIL);
            }

            ArrayList<String> response = retrieveResponse(conn);

            return new MitakeSmsQueryAccountPointResult(response);
        } catch (Exception e) {
            LOG.error(e.getMessage());

            return (MitakeSmsQueryAccountPointResult) connectionError(ConnectionResult.EXCEPTION);
        } finally {
            closeConnection(conn);
        }
    }

    public MitakeSmsQueryMessageStatusResult queryMessageStatus(String... messageIds) {
        HttpURLConnection conn = null;

        try {
            URL url = buildQueryMessageStatusUrl(messageIds);

            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                return (MitakeSmsQueryMessageStatusResult) connectionError(ConnectionResult.FAIL);
            }

            ArrayList<String> response = retrieveResponse(conn);

            return new MitakeSmsQueryMessageStatusResult(response);
        } catch (Exception e) {
            LOG.error(e.getMessage());

            return (MitakeSmsQueryMessageStatusResult) connectionError(ConnectionResult.EXCEPTION);
        } finally {
            closeConnection(conn);
        }
    }

    private ArrayList<String> retrieveResponse(HttpURLConnection conn) throws Exception {
        InputStream is = conn.getInputStream();

        ArrayList<String> response = (ArrayList<String>) IOUtils.readLines(is, "big5");

        is.close();

        LOG.debug("response: {}", response);

        return response;
    }

    private void closeConnection(HttpURLConnection conn) {
        try {
            if (conn != null) {
                conn.disconnect();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    private MitakeSmsResult connectionError(ConnectionResult connectionResult) {
        return new MitakeSmsResult(connectionResult);
    }

    private URL buildUrl(SendOptions opts) throws Exception {
        HashMap<String, String> map = buildUsernameAndPassword();

        map.put(KEY_DESTINATION, StringUtils.join(opts.getDestinations(), ","));
        map.put(KEY_ENCODING, "UTF8");
        map.put(KEY_MESSAGE, encode(opts.getMessage(), map.get(KEY_ENCODING)));

        return getUrl(SEND_URL, map);
    }

    private URL buildQueryMessageStatusUrl(String... messageIds) throws Exception {
        HashMap<String, String> map = buildUsernameAndPassword();

        map.put(KEY_MSG_ID, StringUtils.join(messageIds, ","));

        return getUrl(QUERY_URL, map);
    }

    private URL buildQueryAccountPointUrl() throws Exception {
        HashMap<String, String> map = buildUsernameAndPassword();

        return getUrl(QUERY_URL, map);
    }

    private HashMap<String, String> buildUsernameAndPassword() {
        HashMap<String, String> map = new HashMap<String, String>();

        map.put(KEY_USERNAME, MitakeSms.getUsername());
        map.put(KEY_PASSWORD, MitakeSms.getPassword());

        return map;
    }

    private URL getUrl(String destUrl, HashMap<String, String> map) throws MalformedURLException {
        StringBuffer sb = new StringBuffer();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }

        return new URL(destUrl + "?" + StringUtils.removeEnd(sb.toString(), "&"));
    }

    private String encode(String message, String encoding) throws Exception {
        message = URLEncoder.encode(message, encoding);

        message = message.replace("+", "%20");

        return message;
    }
}