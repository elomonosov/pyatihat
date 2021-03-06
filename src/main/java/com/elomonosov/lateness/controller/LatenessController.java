package com.elomonosov.lateness.controller;

import com.elomonosov.lateness.model.SlackResponse;
import com.elomonosov.lateness.model.User;
import com.elomonosov.lateness.service.LatenessService;
import com.elomonosov.lateness.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/lateness")
public class LatenessController {

    private final static Logger logger = LoggerFactory.getLogger(LatenessController.class);

    private LatenessService latenessService;

    private UserService userService;

    public LatenessController(LatenessService latenessService, UserService userService) {
        this.latenessService = latenessService;
        this.userService = userService;
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
        response.setText("Debt for " + userService.resolveLogin(debtorName) + " is: " + latenessService.getDebt(debtorName).toString());

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
            logger.info("user_name " + userName + "(" + userId + ") confirmed a tribute: user is " + debtorName + ", sum is " + value);
            response.setResponseType(SlackResponse.ResponseType.IN_CHANNEL);
            response.setText("A payment has been added, sum is " + value + ". Actual leftover debt for for @" + debtorName + " is: " + latenessService.payDebt(debtorName, value, userName).toString());
        } else {
            response.setText("Not authorized");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/cancel",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<SlackResponse> cancelSum(@RequestParam("team_domain") String teamDomain,
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
            logger.info("user_name " + userName + "(" + userId + ") confirmed a cancel: " + debtorName + ", sum is " + value);
            Integer leftover = latenessService.cancelDebt(debtorName, value, userName);
            response.setResponseType(SlackResponse.ResponseType.IN_CHANNEL);
            response.setText("A debt has been decreased by " + value + ". Actual leftover debt for for @" + debtorName + " is: " + leftover.toString());

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

        Map<String, Integer> balance = latenessService.getBalance();
        Integer sum = balance.values().stream().reduce(0, Integer::sum);

        String formattedBalance = balance
                .entrySet()
                .stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(System.lineSeparator()));

        response.setText("Overall result is " + sum + ": " + System.lineSeparator() + formattedBalance);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private boolean isAuthorized(String userId) {
        logger.info("Checking " + userId);
        return (userId.equals("U9V6JA53P")) // Igor
                || (userId.equals("UC7JD0PTK")) // Natasha
                || (userId.equals("UHTJ6H487")); // Evgenii
    }

    @RequestMapping(value = "/help",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<SlackResponse> help() {

        SlackResponse response = new SlackResponse();
        response.setText("/500_late @name\n" +
                "если не указать @name, то пишет долг в 500 на отправителя, иначе на указанный username\n" +
                "/500_tribute @name сумма\n" +
                "если не указать @name, то спишет платеж в указанную сумму у отправителя, иначе у указанного username\n" +
                "/500_balance @name\n" +
                "если не указать @name, то выводит баланс отправителя, иначе баланс указанного username\n" +
                "/500_shamehall\n" +
                "выводит список должников и сумм, сортированный по суммам\n" +
                "/cancel @name сумма\n" +
                "если не указать @name, то отменит начисление долга в указанную сумму у отправителя, иначе у указанного username\n" +
                "/500_help\n" +
                "выведет справку по командам\n"
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/user",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> createUser(@RequestBody User user,
                                           @RequestHeader(name = "auth", required = false) String auth) {
        if ((auth != null) && (auth.equals("ololo"))) {
            return new ResponseEntity<>(userService.createUser(user.getLogin(), user.getName()), HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

    }

}
