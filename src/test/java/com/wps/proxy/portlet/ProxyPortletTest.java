package com.wps.proxy.portlet;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.wps.proxy.portlet.ProxyPortlet;
import com.wps.proxy.portlet.business.WebServiceClientHTTPS;

import co.com.ath.payments.mc.service.model.json.BankInfoType;
import co.com.ath.payments.mc.service.model.json.PayCentralBillRequest;
import co.com.ath.payments.mc.service.model.json.PayCentrallBillResponse;

public class ProxyPortletTest {

    final private static Logger logger = LoggerFactory.getLogger(ProxyPortlet.class);

    @Before
    public void setup() {
        System.setProperty("myproperty", "foo");
    }

    @Test
    public void testPublisher() {
        BankInfoType bankInfoType = new BankInfoType();
        PayCentralBillRequest requestPay = new PayCentralBillRequest();
        requestPay.setAgreementID("0091");
        requestPay.setBankID("00010690");
        requestPay.setIpAddress("127.0.0");
        requestPay.setRequestDate(new Date());
        requestPay.setRequestSender("AVAL");
        requestPay.setRequestPage("AVAL");
        requestPay.setRequestOriginPortal("AVAL");
        requestPay.setBankInfo(bankInfoType);
        requestPay.setRequestID("123");
        requestPay.setRequestUser("carloscardona");
        try {
            logger.info("[REQUEST - PayCentrallBill] DATA: " + new Gson().toJson(requestPay));
            // String EndPointRESTServicePayCentralBillRequest = ConfigurationService.getInstance().getEndpointRest();
            String EndPointRESTServicePayCentralBillRequest = "https://asmed44.pragma.com.co:37443/ath.portalpagos.core.transacciones.ws/rest/facturas/";
            PayCentrallBillResponse responsePay = (PayCentrallBillResponse) WebServiceClientHTTPS.getInstance(PayCentrallBillResponse.class)
                    .procesarRequest(EndPointRESTServicePayCentralBillRequest + "obtenerFacturasPorCategorias", requestPay);
            logger.info("[RESPONSE - PayCentrallBill] DATA: " + new Gson().toJson(responsePay));
        } catch (Exception e) {
            logger.error("Error testPublisher", e);
        }
    }
}
