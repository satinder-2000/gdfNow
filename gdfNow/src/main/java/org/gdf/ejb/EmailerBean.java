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
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.gdf.model.EmailMessage;
import java.util.Formatter;

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
    
    @Resource(name="PasswordResetURI")
    String passwordResetURI;

    @Inject
    ReferenceDataBeanLocal referenceDataBeanLocal;

    HashMap<String, List<EmailMessage>> emailMessages;

    @PostConstruct
    public void init() {
        LOGGER.log(Level.INFO, "Web URI set to {0}", webURI);
        LOGGER.log(Level.INFO, "Mail Session Established {0}", session.toString());
        emailMessages= referenceDataBeanLocal.getEmailMessages();

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
        
        List<EmailMessage> regMessages=emailMessages.get(EmailTemplateType.DEEDER_REGISTER.name());
        StringBuilder sb=new StringBuilder(deeder.getEmail()+",\n");
            Map<String, String> map=new HashMap();
            for (EmailMessage msg:regMessages){
                map.put(msg.getMessageTitle(), msg.getText());
            }
            //IN the email we need values in the following order
            //registrationDeeder, successfullyReg, setPassword, createAccess
            sb.append(map.get("registrationDeeder")).append("\n");
            sb.append(map.get("successfullyReg")).append("\n");
            sb.append(map.get("setPassword")).append("\n");
            sb.append(protocol).append(webURI).append(accessConfirmURI).append(deeder.getEmail());
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(deeder.getEmail()));
            message.setSubject(map.get("subject"));
            message.setContent(sb.toString(), "text/plain; charset=utf-8");
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
        List<EmailMessage> accessMessages=emailMessages.get(EmailTemplateType.BUSINESS_REGISTER.name());
        Map<String, String> map=new HashMap();
        for (EmailMessage msg:accessMessages){
            map.put(msg.getMessageTitle(), msg.getText());
        }
        StringBuilder sb=new StringBuilder();
        sb.append(map.get("subject")).append("\n");
        String str1=String.format(map.get("dearBusiness"),business.getEmail());
        System.out.println(str1);
        sb.append(str1).append("\n");
        String str2=String.format(map.get("successfullyReg"),business.getName());
        System.out.println(str2);
        sb.append(str2).append("\n");
        sb.append(map.get("setPasswordLinkMsg")).append("\n");
        sb.append(protocol).append(webURI).append(accessConfirmURI).append(business.getEmail());
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(business.getEmail()));
            message.setSubject(map.get("subject"));
            message.setContent(sb.toString(), "text/plain; charset=utf-8");
            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
    }

    public void sendGovernmentRegConfirmEmail(Government government) {
        List<EmailMessage> accessMessages=emailMessages.get(EmailTemplateType.GOVERNMENT_REGISTER.name());
        Map<String, String> map=new HashMap();
        for (EmailMessage msg:accessMessages){
            map.put(msg.getMessageTitle(), msg.getText());
        }
        StringBuilder sb=new StringBuilder();
        sb.append(map.get("subject")).append("\n");
        sb.append(String.format(map.get("dearGovernment"),government.getEmail())).append("\n");
        sb.append(String.format(map.get("successfullyReg"),government.getOfficeName())).append("\n");
        sb.append(map.get("setPasswordLinkMsg")).append("\n");
        sb.append(protocol).append(webURI).append(accessConfirmURI).append(government.getEmail());
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(government.getEmail()));
            message.setSubject(map.get("subject"));
            message.setContent(sb.toString(), "text/plain; charset=utf-8");
            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }

    }

    public void sendNgoRegConfirmEmail(Ngo ngo) {
        List<EmailMessage> accessMessages=emailMessages.get(EmailTemplateType.NGO_REGISTER.name());
        Map<String, String> map=new HashMap();
        for (EmailMessage msg:accessMessages){
            map.put(msg.getMessageTitle(), msg.getText());
        }
        StringBuilder sb=new StringBuilder();
        sb.append(map.get("subject")).append("\n");
        sb.append(String.format(map.get("dearNgo"),ngo.getEmail())).append("\n");
        sb.append(String.format(map.get("successfullyReg"),ngo.getName())).append("\n");
        sb.append(map.get("setPasswordLinkMsg")).append("\n");
        sb.append(protocol).append(webURI).append(accessConfirmURI).append(ngo.getEmail());
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(ngo.getEmail()));
            message.setSubject(map.get("subject"));
            message.setContent(sb.toString(), "text/plain; charset=utf-8");
            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
    }

    public void sendUserDeederRegConfirmEmail(User user, Deeder deeder) {
        List<EmailMessage> accessMessages=emailMessages.get(EmailTemplateType.USER_DEEDER_REGISTER.name());
        Map<String, String> map=new HashMap();
        for (EmailMessage msg:accessMessages){
            map.put(msg.getMessageTitle(), msg.getText());
        }
        StringBuilder sb=new StringBuilder();
        sb.append(map.get("subject")).append("\n");
        sb.append(String.format(map.get("dearUserDeeder"),deeder.getEmail())).append("\n");
        String username=user.getFirstname()+" "+user.getLastname();
        sb.append(String.format(map.get("nominatedMsg"),username)).append("\n");
        sb.append(map.get("checkProfile")).append("\n");
        sb.append(protocol).append(webURI).append("/view/ViewDeederDetails.xhtml?deederId=").append(deeder.getId()).append("\n");
        sb.append(map.get("profileOKMsg")).append("\n");
        sb.append(protocol).append(webURI).append(accessConfirmURI).append(deeder.getEmail());
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(deeder.getEmail()));
            message.setSubject(map.get("subject"));
            message.setContent(sb.toString(), "text/plain; charset=utf-8");
            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }

    }

    public void sendBusinessAmendedEmail(Business business) {
        List<EmailMessage> accessMessages=emailMessages.get(EmailTemplateType.BUSINESS_AMEND.name());
        Map<String, String> map=new HashMap();
        for (EmailMessage msg:accessMessages){
            map.put(msg.getMessageTitle(), msg.getText());
        }
        StringBuilder sb=new StringBuilder();
        sb.append(map.get("subject")).append("\n");
        sb.append(String.format(map.get("changesApplied"),business.getName())).append("\n");
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(business.getEmail()));
            message.setSubject(map.get("subject"));
            message.setContent(sb.toString(), "text/plain; charset=utf-8");
            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }

    }

    public void sendGovernmentAmendedEmail(Government government) {
        List<EmailMessage> accessMessages=emailMessages.get(EmailTemplateType.GOVERNMENT_AMEND.name());
        Map<String, String> map=new HashMap();
        for (EmailMessage msg:accessMessages){
            map.put(msg.getMessageTitle(), msg.getText());
        }
        StringBuilder sb=new StringBuilder();
        sb.append(map.get("subject")).append("\n");
        sb.append(String.format(map.get("changesApplied"),government.getOfficeName())).append("\n");
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(government.getEmail()));
            message.setSubject(map.get("subject"));
            message.setContent(sb.toString(), "text/plain; charset=utf-8");
            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
    }

    public void sendNgoAmendedEmail(Ngo ngo) {

        List<EmailMessage> accessMessages=emailMessages.get(EmailTemplateType.NGO_AMEND.name());
        Map<String, String> map=new HashMap();
        for (EmailMessage msg:accessMessages){
            map.put(msg.getMessageTitle(), msg.getText());
        }
        StringBuilder sb=new StringBuilder();
        sb.append(map.get("subject")).append("\n");
        sb.append(String.format(map.get("changesApplied"),ngo.getName())).append("\n");
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(ngo.getEmail()));
            message.setSubject(map.get("subject"));
            message.setContent(sb.toString(), "text/plain; charset=utf-8");
            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }

    }

    public void sendDeederNominationConfirmEmail(User user, Deeder deeder) {
                
        List<EmailMessage> regMessages=emailMessages.get(EmailTemplateType.NOMINATION_CONFIRM.name());
        
        StringBuilder sb=new StringBuilder(user.getEmail()+",\n");
        Map<String, String> map=new HashMap();
        for (EmailMessage msg:regMessages){
                map.put(msg.getMessageTitle(), msg.getText());
        }
        //IN the email we need values in the following order
        //registrationDeeder, successfullyReg, setPassword, createAccess
        sb.append(String.format(map.get("subject"),deeder.getFirstname(), deeder.getLastname())).append("\n");
        sb.append(map.get("ThanksMsg")).append("\n");
        sb.append(map.get("suggestDeedOfDeeder")).append("\n");
            
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
            message.setSubject(String.format(map.get("subject"),deeder.getFirstname(), deeder.getLastname()));
            message.setContent(sb.toString(), "text/plain; charset=utf-8");
            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
    }

    public void notifyDeederOfBusinessOffer(BusinessOffer bOffer) {
        Deeder deeder=bOffer.getDeed().getDeeder();
        List<EmailMessage> regMessages=emailMessages.get(EmailTemplateType.DEEDER_NOTIFY_BUSINESS_OFFER.name());
        
        StringBuilder sb=new StringBuilder(deeder.getEmail()+",\n");
        Map<String, String> map=new HashMap();
        for (EmailMessage msg:regMessages){
                map.put(msg.getMessageTitle(), msg.getText());
        }
        String subject=String.format(map.get("subject"), bOffer.getBusiness().getName());
        sb.append(subject).append("\n");
        String congratsMsg=String.format(map.get("congratsMsg"), deeder.getEmail(),bOffer.getBusiness().getName());
        sb.append(congratsMsg).append("\n");
        sb.append(map.get("viewOfferLinkMsg")).append("\n");
        sb.append(protocol).append(webURI).append("/view/ViewBusinessOfferDetails.xhtml?offerId=").append(bOffer.getId());
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(deeder.getEmail()));
            message.setSubject(subject);
            message.setContent(sb.toString(), "text/plain; charset=utf-8");
            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
        
    }

    public void notifyDeederOfGovernmentOffer(GovernmentOffer gOffer) {
        Deeder deeder=gOffer.getDeed().getDeeder();
        List<EmailMessage> regMessages=emailMessages.get(EmailTemplateType.DEEDER_NOTIFY_GOVERNMENT_OFFER.name());
        
        StringBuilder sb=new StringBuilder(deeder.getEmail()+",\n");
        Map<String, String> map=new HashMap();
        for (EmailMessage msg:regMessages){
                map.put(msg.getMessageTitle(), msg.getText());
        }
        sb.append(map.get("subject")).append("\n");
        sb.append(map.get("congratsMsg")).append("\n");
        sb.append(map.get("viewOfferLinkMsg")).append("\n");
        sb.append(protocol).append(webURI).append("/view/ViewGovernmentOfferDetails.xhtml?offerId=").append(gOffer.getId());
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(deeder.getEmail()));
            message.setSubject(map.get("subject"));
            message.setContent(sb.toString(), "text/plain; charset=utf-8");
            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
    }

    public void notifyDeederOfNgoOffer(NgoOffer nOffer) {
        Deeder deeder = nOffer.getDeed().getDeeder();
        List<EmailMessage> regMessages = emailMessages.get(EmailTemplateType.DEEDER_NOTIFY_NGO_OFFER.name());

        StringBuilder sb = new StringBuilder(deeder.getEmail() + ",\n");
        Map<String, String> map = new HashMap();
        for (EmailMessage msg : regMessages) {
            map.put(msg.getMessageTitle(), msg.getText());
        }
        sb.append(map.get("subject")).append("\n");
        sb.append(map.get("congratsMsg")).append("\n");
        sb.append(map.get("viewOfferLinkMsg")).append("\n");
        sb.append(protocol).append(webURI).append("/view/ViewNgoOfferDetails.xhtml?offerId=").append(nOffer.getId());
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(deeder.getEmail()));
            message.setSubject(map.get("subject"));
            message.setContent(sb.toString(), "text/plain; charset=utf-8");
            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
    }
        
    

    public void notifyBusinessOfOffer(BusinessOffer businessOffer) {
        
        List<EmailMessage> regMessages = emailMessages.get(EmailTemplateType.BUSINESS_OFFER.name());

        StringBuilder sb = new StringBuilder(businessOffer.getBusiness().getEmail() + ",\n");
        Map<String, String> map = new HashMap();
        for (EmailMessage msg : regMessages) {
            map.put(msg.getMessageTitle(), msg.getText());
        }
        sb.append(map.get("subject")).append("\n");
        sb.append(String.format(map.get("dearBusiness"), businessOffer.getBusiness().getName())).append("\n");
        sb.append(map.get("thanksMsg")).append("\n");
        sb.append(map.get("visitOfferLinkMsg")).append("\n");
        sb.append(protocol).append(webURI).append("/view/ViewBusinessOfferDetails.xhtml?offerId=").append(businessOffer.getId());
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(businessOffer.getBusiness().getEmail()));
            message.setSubject(map.get("subject"));
            message.setContent(sb.toString(), "text/plain; charset=utf-8");
            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
    }
    
    public void notifyGovernmentOfOffer(GovernmentOffer governmentOffer) {
        List<EmailMessage> regMessages = emailMessages.get(EmailTemplateType.GOVERNMENT_OFFER.name());

        StringBuilder sb = new StringBuilder(governmentOffer.getGovernment().getEmail() + ",\n");
        Map<String, String> map = new HashMap();
        for (EmailMessage msg : regMessages) {
            map.put(msg.getMessageTitle(), msg.getText());
        }
        sb.append(map.get("subject")).append("\n");
        sb.append(String.format(map.get("dearGovernment"), governmentOffer.getGovernment().getOfficeName())).append("\n");
        sb.append(map.get("thanksMsg")).append("\n");
        sb.append(map.get("visitOfferLinkMsg")).append("\n");
        sb.append(protocol).append(webURI).append("/view/ViewGovernmentOfferDetails.xhtml?offerId=").append(governmentOffer.getId());
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(governmentOffer.getGovernment().getEmail()));
            message.setSubject(map.get("subject"));
            message.setContent(sb.toString(), "text/plain; charset=utf-8");
            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
    }
    
    public void notifyNgoOfOffer(NgoOffer ngoOffer) {
        List<EmailMessage> regMessages = emailMessages.get(EmailTemplateType.NGO_OFFER.name());

        StringBuilder sb = new StringBuilder(ngoOffer.getNgo().getEmail() + ",\n");
        Map<String, String> map = new HashMap();
        for (EmailMessage msg : regMessages) {
            map.put(msg.getMessageTitle(), msg.getText());
        }
        sb.append(map.get("subject")).append("\n");
        sb.append(String.format(map.get("dearNgo"), ngoOffer.getNgo().getName())).append("\n");
        sb.append(map.get("thanksMsg")).append("\n");
        sb.append(map.get("visitOfferLinkMsg")).append("\n");
        sb.append(protocol).append(webURI).append("/view/ViewNgoOfferDetails.xhtml?offerId=").append(ngoOffer.getId());
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(ngoOffer.getNgo().getEmail()));
            message.setSubject(map.get("subject"));
            message.setContent(sb.toString(), "text/plain; charset=utf-8");
            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
    }
    
    public void sendDeedCreated(Deeder deeder, Deed deed){
        List<EmailMessage> regMessages = emailMessages.get(EmailTemplateType.CREATE_DEED.name());

        StringBuilder sb = new StringBuilder(deeder.getEmail() + ",\n");
        Map<String, String> map = new HashMap();
        for (EmailMessage msg : regMessages) {
            map.put(msg.getMessageTitle(), msg.getText());
        }
        sb.append(map.get("subject")).append("\n");
        String deederName=deeder.getFirstname()+" "+deeder.getLastname();
        sb.append(String.format(map.get("dearDeeder"), deederName)).append("\n");
        sb.append(map.get("successMsg")).append("\n");
        sb.append(map.get("viewDeedLinkMsg")).append("\n");
        sb.append(protocol).append(webURI).append("/view/ViewDeederDeedDetails.xhtml?deedId=").append(deed.getId());
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(deeder.getEmail()));
            message.setSubject(map.get("subject"));
            message.setContent(sb.toString(), "text/plain; charset=utf-8");
            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
        
    }
    
    public void sendDeedCreated(Ngo ngo, Deed deed){
        List<EmailMessage> regMessages = emailMessages.get(EmailTemplateType.CREATE_DEED_NGO.name());

        StringBuilder sb = new StringBuilder(ngo.getEmail() + ",\n");
        Map<String, String> map = new HashMap();
        for (EmailMessage msg : regMessages) {
            map.put(msg.getMessageTitle(), msg.getText());
        }
        sb.append(map.get("subject")).append("\n");
        sb.append(String.format(map.get("dearNgo"), ngo.getName())).append("\n");
        sb.append(map.get("successMsg")).append("\n");
        sb.append(map.get("viewDeedLinkMsg")).append("\n");
        sb.append(protocol).append(webURI).append("/view/ViewDeederDeedDetails.xhtml?deedId=").append(deed.getId());
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(ngo.getEmail()));
            message.setSubject(map.get("subject"));
            message.setContent(sb.toString(), "text/plain; charset=utf-8");
            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException mex) {
            LOGGER.severe(mex.getMessage());
        }
        
    }

    public void setPasswordResetEmail(String email) {
        List<EmailMessage> regMessages = emailMessages.get(EmailTemplateType.PASSWORD_RESET.name());

        StringBuilder sb = new StringBuilder();
        Map<String, String> map = new HashMap();
        for (EmailMessage msg : regMessages) {
            map.put(msg.getMessageTitle(), msg.getText());
        }
        sb.append(map.get("subject")).append("\n");
        sb.append(map.get("pwResetMsg")).append("\n");
        sb.append(map.get("pwResetLinkMsg")).append("\n");
        sb.append(protocol).append(webURI).append(passwordResetURI);
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
}
