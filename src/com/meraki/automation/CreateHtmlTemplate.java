package com.meraki.automation;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

import java.io.IOException;

/**
 * Created by miguelmorales on 2/11/16.
 */
public class CreateHtmlTemplate {
    Configuration config = new Configuration(Configuration.VERSION_2_3_22);

    public Template CreateTemplate() {

        config.setClassForTemplateLoading(this.getClass(), "Report");
        config.setDefaultEncoding("UTF-8");
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);


    /* Get the template (uses cache internally) */
        Template temp = null;
        try {
            temp = config.getTemplate("ReportTemplate.ftl");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return temp;

    }

}
