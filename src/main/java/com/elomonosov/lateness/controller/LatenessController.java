package com.elomonosov.lateness.controller;

import com.elomonosov.lateness.model.SlackResponse;
import com.elomonosov.lateness.service.LatenessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/lateness")
public class LatenessController {

    private final static Logger logger = LoggerFactory.getLogger(LatenessController.class);

    private LatenessService latenessService;

    public LatenessController(LatenessService latenessService) {
        this.latenessService = latenessService;
    }

    @RequestMapping(
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<SlackResponse> addDebt(
            @RequestParam("team_domain") String teamDomain,
            @RequestParam("channel_id") String channelId,
            @RequestParam("channel_name") String channelName,
            @RequestParam("user_id") String userId,
            @RequestParam("user_name") String userName,
            @RequestParam("command") String command,
            @RequestParam("text") String text,
            @RequestParam("response_url") String responseUrl) {

        String debtorName = getCorrectName(text, userName);

        SlackResponse response = new SlackResponse();
        response.setResponseType(SlackResponse.ResponseType.IN_CHANNEL);
        response.setText("A lateness for @" + debtorName + " has been added. The overall debt is: " + latenessService.addDebt(debtorName, 500, userName).toString());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/balance",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<SlackResponse> getBalance(@RequestParam("team_domain") String teamDomain,
                                                    @RequestParam("channel_id") String channelId,
                                                    @RequestParam("channel_name") String channelName,
                                                    @RequestParam("user_id") String userId,
                                                    @RequestParam("user_name") String userName,
                                                    @RequestParam("command") String command,
                                                    @RequestParam("text") String text,
                                                    @RequestParam("response_url") String responseUrl) {

        String debtorName = getCorrectName(text, userName);
        SlackResponse response = new SlackResponse();
        response.setText("Debt for " + debtorName + " is: " + latenessService.getDebt(debtorName).toString());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String getCorrectName(String incomingName, String userName) {
        if (incomingName.isBlank()) {
            return userName;
        } else {
            return incomingName.substring(1);
        }
    }

    @RequestMapping(value = "/payment",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<SlackResponse> addPayment(@RequestParam("team_domain") String teamDomain,
                                                    @RequestParam("channel_id") String channelId,
                                                    @RequestParam("channel_name") String channelName,
                                                    @RequestParam("user_id") String userId,
                                                    @RequestParam("user_name") String userName,
                                                    @RequestParam("command") String command,
                                                    @RequestParam("text") String text,
                                                    @RequestParam("response_url") String responseUrl) {
        SlackResponse response = new SlackResponse();

        if (isAuthorized(userId)) {
            String[] commands = text.split(" ");
            if (commands.length != 2) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            String debtorName = commands[0].substring(1);
            int value = Integer.parseInt(commands[1]);
            logger.info("user_name " + userName + "(" + userId + ") confirmed a tribute: user is " + debtorName + ", sum is " + value );
            response.setResponseType(SlackResponse.ResponseType.IN_CHANNEL);
            response.setText("A payment for @" + debtorName + " has been added. Actual leftover debt for is: " + latenessService.payDebt(debtorName, value, userName).toString());
        } else {
            response.setText("Not authorized");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/rating",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<SlackResponse> getRating(@RequestParam("team_domain") String teamDomain,
                                                   @RequestParam("channel_id") String channelId,
                                                   @RequestParam("channel_name") String channelName,
                                                   @RequestParam("user_id") String userId,
                                                   @RequestParam("user_name") String userName,
                                                   @RequestParam("command") String command,
                                                   @RequestParam("text") String text,
                                                   @RequestParam("response_url") String responseUrl) {

        SlackResponse response = new SlackResponse();
        response.setText("Overall result is " + latenessService.getBalance().toString());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private boolean isAuthorized(String userId) {
        return (userId.equals("U9V6JA53P")) // Igor
                || (userId.equals("UC7JD0PTK")); // Natasha
    }
}
