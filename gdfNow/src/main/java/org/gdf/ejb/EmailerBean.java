/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.ejb;

import org.gdf.model.Business;
import org.gdf.model.BusinessOffer;
import org.gdf.model.Deed;
import org.gdf.model.Deeder;
import org.gdf.model.EmailTemplateType;
import org.gdf.model.Government;
import org.gdf.model.GovernmentOffer;
import org.gdf.model.Ngo;
import org.gdf.model.NgoOffer;
import org.gdf.model.User;
import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.inject.Inject;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.gdf.model.EmailMessage;

/**
 *
 * @author satindersingh
 */
@Singleton
@LocalBean
public class EmailerBean {

    static final Logger LOGGER = Logger.getLogger(EmailerBean.class.getCanonicalName());

    @Resource(name = "mail/gdf")
    Session session;
    
    @Resource(name = "protocol")
    String protocol;
    
    @Resource(name = "WebURI")
    String webURI;
    
    @Resource(name = "accessConfirmURI")
    String accessConfirmURI;
    
    @Resource(name= "welcomeURI")
    String welcomeURI;
    
    @Resource(name ="webURIView")
    String webURIView;
    
    @Resource(name = "WebURIfaces")
    String WebURIfaces;

    @Resource(name = "genericURI")
    String genericURI;

    @Resource(name = "userDeederURI")
    String userDeederURI;

    @Resource(name = "sender")
    String sender;

    @Resource(name = "emailTemplate")
    String emailTemplate;
    
    @Resource(name="PasswordReset")
    String passwordReset;

    @Inject
    ReferenceDataBeanLocal referenceDataBeanLocal;

    Map<EmailTemplateType, String> templatesMap;
    HashMap<String, List<EmailMessage>> emailMessages;

    @PostConstruct
    public void init() {
        LOGGER.log(Level.INFO, "Web URI set to {0}", webURI);
        LOGGER.log(Level.INFO, "Mail Session Established {0}", session.toString());
        templatesMap = new HashMap<>();
        templatesMap = referenceDataBeanLocal.getEmailTemplatesMap();
        emailMessages= referenceDataBeanLocal.getEmailMessages();

    }

    public void testEmail() {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress("satinder_2000@outlook.com", "Satinder Singh"));
            message.setSubject("Please confirm Email and set Password");

            String htmlText = templatesMap.get(EmailTemplateType.USER_REGISTER);
            VelocityEngine ve = new VelocityEngine();
            StringWriter sw = new StringWriter();
            VelocityContext vc = new VelocityContext();
            vc.put("heading", "Registeration Confirmed");
            vc.put("message", "You have been Registered");
            ve.evaluate(vc, sw, EmailTemplateType.USER_REGISTER.toString(), htmlText);

            message.setContent(sw.getBuffer().toString(), "text/html");

            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (Exception mex) {
            LOGGER.severe(mex.getMessage());
        }

    }

    public void sendUserRegConfirmEmail(User user) {
        List<EmailMessage> regMessages=emailMessages.get(EmailTemplateType.USER_REGISTER.name());
        StringBuilder sb=new StringBuilder(user.getEmail()+",\n");
            Map<String, String> map=new HashMap();
            for (EmailMessage msg:regMessages){
                map.put(msg.getMessageTitle(), msg.getText());
            }
            //IN the email we need values in the following order
            //registrationUser, successfullyReg, setPassword, createAccess
            sb.append(map.get("registrationUser")).append("\n");
            sb.append(map.get("successfullyReg")).append("\n");
            sb.append(map.get("setPassword")).append("\n");
            sb.append(protocol).append(webURI).append(accessConfirmURI).append(user.getEmail());
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
            message.setSubject(map.get("subject"));
            message.setContent(sb.toString(), "text/plain; charset=utf-8");
            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
        
    }

    public void sendDeederRegConfirmEmail(Deeder deeder) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(deeder.getEmail()));
            message.setSubject("Please confirm Email and set Password");

            String htmlText = templatesMap.get(EmailTemplateType.DEEDER_REGISTER);
            VelocityEngine ve = new VelocityEngine();
            StringWriter sw = new StringWriter();
            VelocityContext vc = new VelocityContext();
            vc.put("deederName", deeder.getFirstname().concat(" ").concat(deeder.getLastname()));
            vc.put("deederEmail", deeder.getEmail());
            vc.put("genericURI", genericURI);
            ve.evaluate(vc, sw, EmailTemplateType.DEEDER_REGISTER.toString(), htmlText);

            message.setContent(sw.getBuffer().toString(), "text/html");

            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }

    }

    public void sendAccessConfirmEmail(String email) {
        List<EmailMessage> accessMessages=emailMessages.get(EmailTemplateType.ACCESS_CONFIRM.name());
        Map<String, String> map=new HashMap();
        for (EmailMessage msg:accessMessages){
            map.put(msg.getMessageTitle(), msg.getText());
        }
        StringBuilder sb=new StringBuilder();
        sb.append(email).append("\n");
        sb.append(map.get("thankYou")).append("\n");
        sb.append(map.get("welcome")).append("\n");
        sb.append(protocol).append(webURI).append(welcomeURI);
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject(map.get("subject"));
            message.setContent(sb.toString(), "text/plain; charset=utf-8");
            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
    }

    public void sendBusinessRegConfirmEmail(Business business) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(business.getEmail()));
            message.setSubject("Please confirm Email and set Password");

            String htmlText = templatesMap.get(EmailTemplateType.BUSINESS_REGISTER);
            VelocityEngine ve = new VelocityEngine();
            StringWriter sw = new StringWriter();
            VelocityContext vc = new VelocityContext();
            vc.put("businessEmail", business.getEmail());
            vc.put("businessName", business.getName());
            vc.put("genericURI", genericURI);
            ve.evaluate(vc, sw, EmailTemplateType.BUSINESS_REGISTER.toString(), htmlText);

            message.setContent(sw.getBuffer().toString(), "text/html");

            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }

    }

    public void sendGovernmentRegConfirmEmail(Government government) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(government.getEmail1()));
            message.addRecipient(Message.RecipientType.CC, new InternetAddress(government.getEmail2()));
            message.setSubject("Please confirm Email and set Password");

            String htmlText = templatesMap.get(EmailTemplateType.GOVERNMENT_REGISTER);
            VelocityEngine ve = new VelocityEngine();
            StringWriter sw = new StringWriter();
            VelocityContext vc = new VelocityContext();
            vc.put("governmentEmail", government.getEmail1());
            vc.put("governmentName", government.getName());
            vc.put("genericURI", genericURI);
            ve.evaluate(vc, sw, EmailTemplateType.GOVERNMENT_REGISTER.toString(), htmlText);

            message.setContent(sw.getBuffer().toString(), "text/html");

            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }

    }

    public void sendNgoRegConfirmEmail(Ngo ngo) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(ngo.getEmail()));
            message.setSubject("Please confirm Email and set Password");

            String htmlText = templatesMap.get(EmailTemplateType.NGO_REGISTER);
            VelocityEngine ve = new VelocityEngine();
            StringWriter sw = new StringWriter();
            VelocityContext vc = new VelocityContext();
            vc.put("ngoEmail", ngo.getEmail());
            vc.put("ngoName", ngo.getName());
            vc.put("genericURI", genericURI);
            ve.evaluate(vc, sw, EmailTemplateType.NGO_REGISTER.toString(), htmlText);

            message.setContent(sw.getBuffer().toString(), "text/html");

            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
    }

    public void sendUserDeederRegConfirmEmail(User user, Deeder deeder) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(deeder.getEmail()));
            message.setSubject(user.getFirstname() + " " + user.getLastname() + " has recommended you");

            String htmlText = templatesMap.get(EmailTemplateType.USER_DEEDER_REGISTER);
            VelocityEngine ve = new VelocityEngine();
            StringWriter sw = new StringWriter();
            VelocityContext vc = new VelocityContext();
            vc.put("webURIView", webURIView);
            vc.put("userName", user.getFirstname().concat(" ").concat(user.getLastname()));
            vc.put("deederId", deeder.getId());
            vc.put("userDeederURI", userDeederURI);
            ve.evaluate(vc, sw, EmailTemplateType.USER_DEEDER_REGISTER.toString(), htmlText);

            message.setContent(sw.getBuffer().toString(), "text/html");

            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }

    }

    public void sendBusinessAmendedEmail(Business business) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(business.getEmail()));
            message.setSubject("Change made to account");

            String htmlText = templatesMap.get(EmailTemplateType.BUSINESS_AMEND);
            VelocityEngine ve = new VelocityEngine();
            StringWriter sw = new StringWriter();
            VelocityContext vc = new VelocityContext();
            vc.put("heading", "Change applied successfully.");
            vc.put("message", "The changes you applied to your account have now been applied.");
            ve.evaluate(vc, sw, EmailTemplateType.BUSINESS_AMEND.toString(), htmlText);

            message.setContent(sw.getBuffer().toString(), "text/html");

            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
    }

    public void sendGovernmentAmendedEmail(Government government) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(government.getEmail1()));
            message.addRecipient(Message.RecipientType.CC, new InternetAddress(government.getEmail2()));
            message.setSubject("Change made to account");

            String htmlText = templatesMap.get(EmailTemplateType.GOVERNMENT_AMEND);
            VelocityEngine ve = new VelocityEngine();
            StringWriter sw = new StringWriter();
            VelocityContext vc = new VelocityContext();
            vc.put("heading", "Change applied successfully.");
            vc.put("message", "The changes you applied to your account have now been applied.");
            ve.evaluate(vc, sw, EmailTemplateType.GOVERNMENT_AMEND.toString(), htmlText);

            message.setContent(sw.getBuffer().toString(), "text/html");

            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
    }

    public void sendNgoAmendedEmail(Ngo ngo) {

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(ngo.getEmail()));
            message.setSubject("Change made to account");

            String htmlText = templatesMap.get(EmailTemplateType.NGO_AMEND);
            VelocityEngine ve = new VelocityEngine();
            StringWriter sw = new StringWriter();
            VelocityContext vc = new VelocityContext();
            vc.put("heading", "Change applied successfully.");
            vc.put("message", "The changes you applied to your account have now been applied.");
            ve.evaluate(vc, sw, EmailTemplateType.NGO_AMEND.toString(), htmlText);

            message.setContent(sw.getBuffer().toString(), "text/html");

            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }

    }

    public void sendDeederNominationConfirmEmail(User user, Deeder deeder) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
            message.setSubject("Nomination of " + deeder.getFirstname() + " " + deeder.getLastname());

            String htmlText = templatesMap.get(EmailTemplateType.NOMINATION_CONFIRM);
            VelocityEngine ve = new VelocityEngine();
            StringWriter sw = new StringWriter();
            VelocityContext vc = new VelocityContext();
            vc.put("userName", user.getFirstname().concat(" ").concat(user.getLastname()));
            vc.put("deederName", deeder.getFirstname().concat(" ").concat(deeder.getLastname()));
            vc.put("webURI", webURI);
            ve.evaluate(vc, sw, EmailTemplateType.NOMINATION_CONFIRM.toString(), htmlText);

            message.setContent(sw.getBuffer().toString(), "text/html");

            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
    }

    public void notifyDeederOfBusinessOffer(BusinessOffer bOffer) {
        Deeder deeder=bOffer.getDeed().getDeeder();
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(deeder.getEmail()));
            message.setSubject("New Offer from Business: " + bOffer.getBusiness().getName());

            String htmlText = templatesMap.get(EmailTemplateType.DEEDER_NOTIFY_BUSINESS_OFFER);
            VelocityEngine ve = new VelocityEngine();
            StringWriter sw = new StringWriter();
            VelocityContext vc = new VelocityContext();
            vc.put("deederName", deeder.getFirstname().concat(" ").concat(deeder.getLastname()));
            vc.put("businessName", bOffer.getBusiness().getName());
            vc.put("offerId", bOffer.getId());
            vc.put("WebURIfaces", WebURIfaces);
            ve.evaluate(vc, sw, EmailTemplateType.DEEDER_NOTIFY_BUSINESS_OFFER.toString(), htmlText);

            message.setContent(sw.getBuffer().toString(), "text/html");

            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
        
    }

    public void notifyDeederOfGovernmentOffer(GovernmentOffer gOffer) {
        Deeder deeder=gOffer.getDeed().getDeeder();
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(deeder.getEmail()));
            message.setSubject("New Offer from : " + gOffer.getGovernment().getName());

            String htmlText = templatesMap.get(EmailTemplateType.DEEDER_NOTIFY_GOVERNMENT_OFFER);
            VelocityEngine ve = new VelocityEngine();
            StringWriter sw = new StringWriter();
            VelocityContext vc = new VelocityContext();
            vc.put("deederName", deeder.getFirstname().concat(" ").concat(deeder.getLastname()));
            vc.put("governmentName", gOffer.getGovernment().getName());
            vc.put("offerId", gOffer.getId());
            vc.put("WebURIfaces", WebURIfaces);
            ve.evaluate(vc, sw, EmailTemplateType.DEEDER_NOTIFY_GOVERNMENT_OFFER.toString(), htmlText);

            message.setContent(sw.getBuffer().toString(), "text/html");

            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
    }

    public void notifyDeederOfNgoOffer(NgoOffer nOffer) {
         Deeder deeder=nOffer.getDeed().getDeeder();
         try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(deeder.getEmail()));
            message.setSubject("New Offer from NGO: " + nOffer.getNgo().getName());

            String htmlText = templatesMap.get(EmailTemplateType.DEEDER_NOTIFY_NGO_OFFER);
            VelocityEngine ve = new VelocityEngine();
            StringWriter sw = new StringWriter();
            VelocityContext vc = new VelocityContext();
            vc.put("deederName", deeder.getFirstname().concat(" ").concat(deeder.getLastname()));
            vc.put("ngoName", nOffer.getNgo().getName());
            vc.put("offerId", nOffer.getId());
            vc.put("WebURIfaces", WebURIfaces);
            ve.evaluate(vc, sw, EmailTemplateType.DEEDER_NOTIFY_NGO_OFFER.toString(), htmlText);

            message.setContent(sw.getBuffer().toString(), "text/html");

            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
    }
        
    

    public void notifyBusinessOfOffer(BusinessOffer businessOffer) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(businessOffer.getBusiness().getEmail()));
            message.setSubject("Offer is now Live!!");

            String htmlText = templatesMap.get(EmailTemplateType.BUSINESS_OFFER);
            VelocityEngine ve = new VelocityEngine();
            StringWriter sw = new StringWriter();
            VelocityContext vc = new VelocityContext();
            vc.put("businessName", businessOffer.getBusiness().getName());
            vc.put("businessEmail", businessOffer.getBusiness().getEmail());
            vc.put("offerId", businessOffer.getId());
            vc.put("WebURIfaces", WebURIfaces);
            ve.evaluate(vc, sw, EmailTemplateType.BUSINESS_OFFER.toString(), htmlText);

            message.setContent(sw.getBuffer().toString(), "text/html");

            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
    }
    
    public void notifyGovernmentOfOffer(GovernmentOffer governmentOffer) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(governmentOffer.getGovernment().getEmail1()));
            message.addRecipient(Message.RecipientType.CC, new InternetAddress(governmentOffer.getGovernment().getEmail2()));
            message.setSubject("Offer is now Live!!");

            String htmlText = templatesMap.get(EmailTemplateType.GOVERNMENT_OFFER);
            VelocityEngine ve = new VelocityEngine();
            StringWriter sw = new StringWriter();
            VelocityContext vc = new VelocityContext();
            vc.put("governmentName", governmentOffer.getGovernment().getName());
            vc.put("governmentEmail1", governmentOffer.getGovernment().getEmail1());
            vc.put("offerId", governmentOffer.getId());
            vc.put("WebURIfaces", WebURIfaces);
            ve.evaluate(vc, sw, EmailTemplateType.GOVERNMENT_OFFER.toString(), htmlText);

            message.setContent(sw.getBuffer().toString(), "text/html");

            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
    }
    
    public void notifyNgoOfOffer(NgoOffer ngoOffer) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(ngoOffer.getNgo().getEmail()));
            message.setSubject("Offer is now Live!!");

            String htmlText = templatesMap.get(EmailTemplateType.NGO_OFFER);
            VelocityEngine ve = new VelocityEngine();
            StringWriter sw = new StringWriter();
            VelocityContext vc = new VelocityContext();
            vc.put("ngoName", ngoOffer.getNgo().getName());
            vc.put("ngoEmail", ngoOffer.getNgo().getEmail());
            vc.put("offerId", ngoOffer.getId());
            vc.put("WebURIfaces", WebURIfaces);
            ve.evaluate(vc, sw, EmailTemplateType.GOVERNMENT_OFFER.toString(), htmlText);

            message.setContent(sw.getBuffer().toString(), "text/html");

            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
    }
    
    public void sendDeedCreated(Deeder deeder, Deed deed){
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(deeder.getEmail()));
            message.setSubject("Deed Registered");

            String htmlText = templatesMap.get(EmailTemplateType.CREATE_DEED);
            VelocityEngine ve = new VelocityEngine();
            StringWriter sw = new StringWriter();
            VelocityContext vc = new VelocityContext();
            vc.put("deederName", deeder.getFirstname().concat(" ").concat(deeder.getLastname()));
            vc.put("WebURIfaces", WebURIfaces);
            vc.put("deedId", deed.getId());
            ve.evaluate(vc, sw, EmailTemplateType.CREATE_DEED.toString(), htmlText);

            message.setContent(sw.getBuffer().toString(), "text/html");

            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
        
    }
    
    public void sendDeedCreated(Ngo ngo, Deed deed){
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(ngo.getEmail()));
            message.setSubject("Deed Registered");

            String htmlText = templatesMap.get(EmailTemplateType.CREATE_DEED_NGO);
            VelocityEngine ve = new VelocityEngine();
            StringWriter sw = new StringWriter();
            VelocityContext vc = new VelocityContext();
            vc.put("ngoName", ngo.getName());
            vc.put("WebURIfaces", WebURIfaces);
            vc.put("deedId", deed.getId());
            ve.evaluate(vc, sw, EmailTemplateType.CREATE_DEED_NGO.toString(), htmlText);

            message.setContent(sw.getBuffer().toString(), "text/html");

            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
        
    }

    public void setPasswordResetEmail(String email) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject("Password Reset");

            String htmlText = templatesMap.get(EmailTemplateType.PASSWORD_RESET);
            VelocityEngine ve = new VelocityEngine();
            StringWriter sw = new StringWriter();
            VelocityContext vc = new VelocityContext();
            vc.put("PasswordReset", passwordReset);
            ve.evaluate(vc, sw, EmailTemplateType.PASSWORD_RESET.toString(), htmlText);

            message.setContent(sw.getBuffer().toString(), "text/html");

            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
    }
}
