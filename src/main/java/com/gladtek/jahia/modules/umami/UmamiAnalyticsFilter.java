package com.gladtek.jahia.modules.umami;

import net.htmlparser.jericho.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.filter.RenderFilter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.*;
import org.slf4j.Logger;

import java.util.List;

@Component(service = RenderFilter.class, immediate = true)
public class UmamiAnalyticsFilter extends AbstractFilter{

    public static final String GLADTEKMIX_UMAMI_ANALYTICS = "gladtekmix:umamiAnalytics";
    private static Logger logger = LoggerFactory.getLogger(UmamiAnalyticsFilter.class);

    private String headScript;
    private boolean isUmamiConfiguredOnSite;

    @Activate
    public void activate() {
        setPriority(3);
        setApplyOnModes("live");
        setSkipOnAjaxRequest(true);
        setApplyOnConfigurations("page");
        setSkipOnConfigurations("include,wrapper");
    }

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        JCRSiteNode site = renderContext.getSite();
        isUmamiConfiguredOnSite = site.isNodeType(GLADTEKMIX_UMAMI_ANALYTICS);
        if(isUmamiConfiguredOnSite) {
            String umamiJsScriptUrl = site.hasProperty("umamiJsScriptUrl") ? site.getProperty("umamiJsScriptUrl").getString() : null;
            String umamiWebSiteId = site.hasProperty("umamiWebSiteId") ? site.getProperty("umamiWebSiteId").getString() : null;
            headScript = "\n<script defer src="+ umamiJsScriptUrl+" data-website-id="+ umamiWebSiteId+"></script>\n<";
        }
        return super.prepare(renderContext, resource, chain);
    }

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        String output = super.execute(previousOut, renderContext, resource, chain);
        if(isUmamiConfiguredOnSite) {
            output = enhanceOutput(output);
        }
        return output;
    }

    /**
     * This Function is just to add some logic to our filter and therefore not needed to declare a filter
     *
     * @param output    Original output to modify
     * @return          Modified output
     */
    private String enhanceOutput(String output) {
        Source source = new Source(output);
        OutputDocument outputDocument = new OutputDocument(source);
        List<Element> elementList = source.getAllElements(HTMLElementName.HEAD);
        if (elementList != null && !elementList.isEmpty()) {
            final EndTag bodyEndTag = elementList.get(0).getEndTag();
            outputDocument.replace(bodyEndTag.getBegin(), bodyEndTag.getBegin() + 1, headScript);
        }
        output = outputDocument.toString().trim();
        return output;
    }


}
