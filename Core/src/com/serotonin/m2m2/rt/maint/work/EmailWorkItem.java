/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.rt.maint.work;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.email.MangoEmailContent;
import com.serotonin.m2m2.email.PostEmailRunnable;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.type.SystemEventType;
import com.serotonin.web.mail.EmailContent;
import com.serotonin.web.mail.EmailSender;

/**
 * @author Matthew Lohbihler
 * 
 */
public class EmailWorkItem implements WorkItem {
    private static final Log LOG = LogFactory.getLog(EmailWorkItem.class);

    @Override
    public int getPriority() {
        return WorkItem.PRIORITY_LOW; //Changed from medium to low as it isn't as important as Data Point Listener callbacks
    }

    public static void queueEmail(String toAddr, MangoEmailContent content) throws AddressException {
        queueEmail(new String[] { toAddr }, content);
    }

    public static void queueEmail(String[] toAddrs, MangoEmailContent content) throws AddressException {
        queueEmail(toAddrs, content, null);
    }

    public static void queueEmail(String[] toAddrs, MangoEmailContent content, PostEmailRunnable[] postSendExecution)
            throws AddressException {
        queueEmail(toAddrs, content.getSubject(), content, postSendExecution);
    }

    public static void queueEmail(String[] toAddrs, String subject, EmailContent content, PostEmailRunnable[] postSendExecution)
            throws AddressException {
        EmailWorkItem item = new EmailWorkItem();

        item.toAddresses = new InternetAddress[toAddrs.length];
        for (int i = 0; i < toAddrs.length; i++)
            item.toAddresses[i] = new InternetAddress(toAddrs[i]);

        item.subject = subject;
        item.content = content;
        item.postSendExecution = postSendExecution;

        Common.backgroundProcessing.addWorkItem(item);
    }

    private InternetAddress fromAddress;
    private InternetAddress[] toAddresses;
    private String subject;
    private EmailContent content;
    private PostEmailRunnable[] postSendExecution;

    @Override
    public void execute() {
    	
    	Exception failedEx = null;
    	boolean success = true;
        try {
            if (fromAddress == null) {
                String addr = SystemSettingsDao.getValue(SystemSettingsDao.EMAIL_FROM_ADDRESS);
                String pretty = SystemSettingsDao.getValue(SystemSettingsDao.EMAIL_FROM_NAME);
                fromAddress = new InternetAddress(addr, pretty);
            }

            EmailSender emailSender = new EmailSender(SystemSettingsDao.getValue(SystemSettingsDao.EMAIL_SMTP_HOST),
                    SystemSettingsDao.getIntValue(SystemSettingsDao.EMAIL_SMTP_PORT),
                    SystemSettingsDao.getBooleanValue(SystemSettingsDao.EMAIL_AUTHORIZATION),
                    SystemSettingsDao.getValue(SystemSettingsDao.EMAIL_SMTP_USERNAME),
                    SystemSettingsDao.getValue(SystemSettingsDao.EMAIL_SMTP_PASSWORD),
                    SystemSettingsDao.getBooleanValue(SystemSettingsDao.EMAIL_TLS));

            emailSender.send(fromAddress, toAddresses, subject, content);
        }
        catch (Exception e) {
            LOG.warn("Error sending email", e);
            failedEx = e;
            success = false;
            String to = "";
            for (InternetAddress addr : toAddresses) {
                if (to.length() > 0)
                    to += ", ";
                to += addr.getAddress();
            }
            SystemEventType.raiseEvent(new SystemEventType(SystemEventType.TYPE_EMAIL_SEND_FAILURE),
                    System.currentTimeMillis(), false,
                    new TranslatableMessage("event.email.failure", subject, to, e.getMessage()));
        }
        finally {
            if (postSendExecution != null) {
                for (PostEmailRunnable runnable : postSendExecution)
                    runnable.emailFinished(success, failedEx);
            }
        }
    }

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.rt.maint.work.WorkItem#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Sending email from " + this.fromAddress.toString() + " about " + this.subject; 
	}
}
