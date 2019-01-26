package com.wps.proxy.portlet;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import com.wps.proxy.portlet.ProxyPortletTest;

import co.com.ath.logger.CustomLogger;

/**
 * A sample portlet
 */
public class ProxyPortlet extends GenericPortlet {

    // private static final Log LOGGER = LoggerFactory.getLogger(ProxyPortlet.class);
    private CustomLogger logger = new CustomLogger(ProxyPortletTest.class);

    /*
     * (non-Javadoc)
     * 
     * @see javax.portlet.GenericPortlet#init(javax.portlet.PortletConfig)
     */
    @Override
    public void init(PortletConfig config) throws PortletException {
        super.init(config);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.portlet.GenericPortlet#doView(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        try {
            String view = "/views/index.jsp";
            getPortletContext().getRequestDispatcher(view).include(request, response);
        } catch (Exception e) {
            logger.error("TextoError", e);
            request.setAttribute("textoError", e.getMessage());
            getPortletContext().getRequestDispatcher("/views/errorPage.jsp").include(request, response);
        }
        super.doView(request, response);
    }

    @Override
    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException, IOException {
        // TODO Auto-generated method stub
        super.processAction(actionRequest, actionResponse);
    }

    @Override
    public void serveResource(ResourceRequest arg0, ResourceResponse arg1) throws PortletException, IOException {
        // TODO Auto-generated method stub
        super.serveResource(arg0, arg1);
    }
}
