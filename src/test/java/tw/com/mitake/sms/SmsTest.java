package tw.com.mitake.sms;

import org.junit.Before;
import org.junit.Test;
import tw.com.mitake.sms.listener.OnPostSendListener;
import tw.com.mitake.sms.listener.OnPreSendListener;
import tw.com.mitake.sms.result.MitakeSmsSendResult;

import static org.junit.Assert.assertNotNull;

public class SmsTest {
    @Before
    public void setUp() {
        MitakeSms.init(System.getenv("MITAKE_SMS_USERNAME"), System.getenv("MITAKE_SMS_PASSWORD"));
    }

    @Test
    public void testSms() {
        MitakeSmsSendResult result = MitakeSms.send(System.getenv("MITAKE_SMS_TO"), "this 簡訊 & 內容 message 1 + 2 羣");

        System.out.println("result: " + result.toString());

        assertNotNull(result);
    }

    @Test
    public void testSmsOnPreSend() {
        MitakeSms.send(System.getenv("MITAKE_SMS_TO"), "OnPreSend message", new OnPreSendListener() {
            public void onPreSend() {
                System.out.println("OnPreSend");
            }
        });
    }

    @Test
    public void testSmsOnPostSend() {
        MitakeSms.send(System.getenv("MITAKE_SMS_TO"), "OnPostSend message", new OnPostSendListener() {
            public void onPostSend() {
                System.out.println("OnPostSend");
            }
        });
    }
}