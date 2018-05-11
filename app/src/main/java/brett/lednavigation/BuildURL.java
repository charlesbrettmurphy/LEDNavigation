package brett.lednavigation;

/**
 * Created by brett on 3/23/2018. Documentation for interfacing with the hue gateway is found
 * here. https://www.developers.meethue.com/documentation/groups-api
 * <p>
 * This class builds the URL to be submitted to the hue gateway.
 */

public class BuildURL {
    private String url;

    BuildURL(String bridgeUserUrl) {
        url = bridgeUserUrl;
    }

    public String getConnectionStatusUrl() {
        url = url.concat("config");
        return url;
    }

    public String getLights()

    {
        url = url.concat("lights");
        return url;
    }


    public String getLightState(int id) {
        url = url.concat("lights/").concat(Integer.toString(id));
        return url;
    }

    public String getAllGroups() {
        url = url.concat("groups/");
        return url;
    }

    public String getGroupAttributesById(int id) {
        url = url.concat("groups/").concat(Integer.toString(id));
        return url;
    }

    public String setLightState(int id) {
        url = url.concat("lights/").concat(Integer.toString(id)).concat("/state");
        return url;
    }

    public String setGroupState(int id) {

        url = url.concat("groups/").concat(Integer.toString(id).concat("/action"));
        return url;
    }

    public String getCapabilities() {
        url = url.concat("capabilities");
        return url;
    }

    public String getNewLights() {
        url = url.concat("lights/new");
        return url;
    }


}
