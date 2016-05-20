package pt.upa.transporter.ws.cli;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.handler.AuthenticationHandler;
import pt.upa.shared.domain.CertificateHelper;
import pt.upa.transporter.exception.TransporterClientException;
import pt.upa.transporter.ws.*;
import sun.awt.AWTIcon32_security_icon_yellow16_png;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;
import java.security.PrivateKey;
import java.util.List;
import java.util.Map;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

/**
 * TransporterClient
 * <p>
 * This class wraps the Port generated by wsimport and adds easier
 * endpoint address configuration.
 * <p>
 * It implements the TransporterPortType interface to provide access to
 * exactly the same operations as the Port generated by wsimport.
 * <p>
 * This class can be either instantiated by providing the pair (UDDI Url, wsName),
 * which will cause it to lookup the endpoint address in UDDI with that name (wsName)
 * or by providing the (endpointAddress) directly.
 */
public final class TransporterClient implements TransporterPortType {

    private static final String DEFAULT_BROKER_NAME = "UpaBroker";
    private final UDDINaming uddiNaming;
    private final String endpointAddress; // A.K.A. wsUrl
    private final String uddiUrl;
    private final String wsName;
    private TransporterService service;
    private TransporterPortType port;
    private BindingProvider bindingProvider;
    private Map<String, Object> requestContext;
    private final String brokerWsName;
    private final PrivateKey brokerPrivateKey;


    public TransporterClient(String uddiURL, String wsName, String brokerWsName, PrivateKey privateKey)
            throws TransporterClientException {
        this.uddiUrl = uddiURL;
        this.wsName = wsName;
        this.brokerWsName = brokerWsName;
        this.brokerPrivateKey = privateKey;
        try {
            //TODO: replace commented out print messages with log messages
            // System.out.printf("Contacting UDDI at %s%n", uddiURL);
            uddiNaming = new UDDINaming(uddiURL);

            //System.out.printf("Searching for '%s'%n", name);
            endpointAddress = uddiNaming.lookup(wsName);

            if (endpointAddress == null) {
                throw new TransporterClientException("'" + wsName + "' Not Found at " + uddiURL);
            } else {
                //System.out.printf("Found %s%n", endpointAddress);
            }
            createStub();
        } catch (JAXRException e) {
            throw new TransporterClientException("UDDI error: " + e.getMessage(), e);
        }
        setReqContext();
    }


    public TransporterClient(String endpointAddress, String brokerWsName, PrivateKey privateKey) {
        uddiNaming = null;
        uddiUrl = null;
        wsName = null;

        this.brokerWsName = brokerWsName;
        this.brokerPrivateKey = privateKey;

        this.endpointAddress = endpointAddress;
        createStub();
        setReqContext();
    }

    /**
     * Instantiate TransporterClient directly from an endpoint address
     *
     * @param endpointAddress the endpoint address of the {@link pt.upa.transporter.ws.TransporterPortType}
     */
    public TransporterClient(String endpointAddress) {
        uddiNaming = null;
        uddiUrl = null;
        wsName = null;

        this.brokerWsName = DEFAULT_BROKER_NAME;

        try {
            this.brokerPrivateKey = CertificateHelper.getPrivateKey(DEFAULT_BROKER_NAME);
        } catch (Exception e) {
            throw new TransporterClientException("UpaBroker PrivateKey not found, make sure the keys have been generated. " +
                    "Please, follow the instructions in README.md");
        }

        this.endpointAddress = endpointAddress;
        createStub();
        setReqContext();
    }

    /**
     * Instantiate from UDDI url and wsName.
     *
     * @param uddiURL UDDI server address
     * @param wsName  name of the transporter to connect to
     * @throws TransporterClientException
     */
    public TransporterClient(String uddiURL, String wsName) throws TransporterClientException {
        this.uddiUrl = uddiURL;
        this.wsName = wsName;

        this.brokerWsName = DEFAULT_BROKER_NAME;

        try {
            this.brokerPrivateKey = CertificateHelper.getPrivateKey(DEFAULT_BROKER_NAME);
        } catch (Exception e) {
            throw new TransporterClientException("UpaBroker PrivateKey not found, make sure the keys have been generated. " +
                    "Please, follow the instructions in README.md");
        }

        try {
            //TODO: replace commented out print messages with log messages
            // System.out.printf("Contacting UDDI at %s%n", uddiURL);
            uddiNaming = new UDDINaming(uddiURL);

            //System.out.printf("Searching for '%s'%n", name);
            endpointAddress = uddiNaming.lookup(wsName);

            if (endpointAddress == null) {
                throw new TransporterClientException("'" + wsName + "' Not Found at " + uddiURL);
            } else {
                //System.out.printf("Found %s%n", endpointAddress);
            }
            createStub();
        } catch (JAXRException e) {
            throw new TransporterClientException("UDDI error: " + e.getMessage(), e);
        }
        setReqContext();
    }

    private void createStub() {
        service = new TransporterService();
        port = service.getTransporterPort();

        bindingProvider = (BindingProvider) port;
        requestContext = bindingProvider.getRequestContext();
        requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
    }


    @Override
    public String ping(String name) {
        setReqContext();
        return port.ping(name);
    }

    @Override
    public JobView requestJob(String origin, String destination, int price) throws BadLocationFault_Exception, BadPriceFault_Exception {
        setReqContext();
        return port.requestJob(origin, destination, price);
    }

    @Override
    public JobView decideJob(String id, boolean accept) throws BadJobFault_Exception {
        setReqContext();
        return port.decideJob(id, accept);
    }

    @Override
    public JobView jobStatus(String id) {
        setReqContext();
        return port.jobStatus(id);
    }

    @Override
    public List<JobView> listJobs() {
        setReqContext();
        return port.listJobs();
    }

    @Override
    public void clearJobs() {
        setReqContext();
        port.clearJobs();
    }

    public UDDINaming getUddiNaming() {
        return uddiNaming;
    }

    public String getEndpointAddress() {
        return endpointAddress;
    }

    public TransporterService getService() {
        return service;
    }

    public TransporterPortType getPort() {
        return port;
    }

    public BindingProvider getBindingProvider() {
        return bindingProvider;
    }

    public Map<String, Object> getRequestContext() {
        return requestContext;
    }


    public String getUddiUrl() {
        return uddiUrl;
    }

    public String getWsName() {
        return wsName;
    }


    private void setReqContext() {
        bindingProvider.getRequestContext().put(AuthenticationHandler.CONTEXT_PRIVATE_KEY, this.brokerPrivateKey);
        bindingProvider.getRequestContext().put(AuthenticationHandler.CONTEXT_SENDER_NAME, this.brokerWsName);
    }

    public void DEMOturnDemoModeOn() {
        bindingProvider.getRequestContext().put(AuthenticationHandler.DEMO_MODE, "true");
    }

    public void DEMOchangeNonce(String nonce) {
        DEMOturnDemoModeOn();
        bindingProvider.getRequestContext().put(AuthenticationHandler.CHANGE_NONCE, nonce);
    }

    public void DEMOFixNonce(String nonce) {
        DEMOturnDemoModeOn();
        bindingProvider.getRequestContext().put(AuthenticationHandler.FIX_NONCE, nonce);
    }
}
