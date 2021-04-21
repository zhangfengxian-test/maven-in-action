package com.zfx.mvnbook.account.service;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import com.zfx.mvnbook.account.captcha.AccountCaptchaService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.mail.Message;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

@ContextConfiguration(classes = ServiceConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class AccountServiceTest {
    private GreenMail greenMail;

    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountCaptchaService accountCaptchaService;

    @Before
    public void prepare()
            throws Exception {
        List<String> preDefinedTexts = new ArrayList<String>();
        preDefinedTexts.add("12345");
        preDefinedTexts.add("abcde");
        accountCaptchaService.setPreDefinedTexts(preDefinedTexts);

        greenMail = new GreenMail(ServerSetup.SMTP);
        greenMail.setUser("test@juvenxu.com", "123456");
        greenMail.start();

        File persistDataFile = new File("target/test-classes/persist-data.xml");
        if (persistDataFile.exists()) {
            persistDataFile.delete();
        }
    }

    @Test
    public void testAccountService()
            throws Exception {
        // 1. Get captcha
        String captchaKey = accountService.generateCaptchaKey();
        accountService.generateCaptchaImage(captchaKey);
        String captchaValue = "12345";

        // 2. Submit sign up Request
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setCaptchaKey(captchaKey);
        signUpRequest.setCaptchaValue(captchaValue);
        signUpRequest.setId("juven");
        signUpRequest.setEmail("test@juvenxu.com");
        signUpRequest.setName("Juven Xu");
        signUpRequest.setPassword("admin123");
        signUpRequest.setConfirmPassword("admin123");
        signUpRequest.setActivateServiceUrl("http://localhost:8080/account/activate");
        accountService.signUp(signUpRequest);

        // 3. Read activation link
        greenMail.waitForIncomingEmail(2000, 1);
        Message[] msgs = greenMail.getReceivedMessages();
        assertEquals(1, msgs.length);
        assertEquals("Please Activate Your Account", msgs[0].getSubject());
        String activationLink = GreenMailUtil.getBody(msgs[0]).trim();

        // 3a. Try login but not activated
        try {
            accountService.login("juven", "admin123");
            fail("Disabled account shouldn't be able to log in.");
        } catch (AccountServiceException e) {
        }

        // 4. Activate account
        String activationCode = activationLink.substring(activationLink.lastIndexOf("=") + 1);
        accountService.activate(activationCode);

        // 5. Login with correct id and password
        accountService.login("juven", "admin123");

        // 5a. Login with incorrect password
        try {
            accountService.login("juven", "admin456");
            fail("Password is incorrect, shouldn't be able to login.");
        } catch (AccountServiceException e) {
        }

    }

    @After
    public void stopMailServer()
            throws Exception {
        greenMail.stop();
    }
}
