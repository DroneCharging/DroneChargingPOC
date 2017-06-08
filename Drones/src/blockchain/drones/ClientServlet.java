package blockchain.drones;

import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.Writer;

/**
 * Servlet that handles requests from the client. When a POST request is
 * sent to this URL, a new Transaction with the specified parameters is
 * created and enqueued to be handled by the corresponding charging pad.
 * The client would then be put on a poll cycle and would periodically
 * send GET to obtain the status of the charging process (whether it is
 * currently being charged or whether the full amount of power purchased has
 * been dispersed). If the GET returns true, then the transaction is complete.
 */

@WebServlet(name = "ClientServlet")
public class ClientServlet extends HttpServlet {
    private static final String ARG_USER = "user";
    private static final String ARG_PAD = "pad";
    private static final String ARG_EXPECTED = "expected";

    private static final String STATUS_COMPLETE = "COMPLETE";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_NONEXISTENT = "NO_TRANSACTION_EXISTS";

    private static final String KEY_STATUS = "status";

    /**
     * Handles the POST request sent to this server. If the correct parameters
     * are given and the Transaction is created successfully, then HTTP "created"
     * code (201) is written back to the user. Else, HTTP "bad request" code (400)
     * is written back. If the transaction is created successfully, then that
     * means the client has received an invoice from PayPal for the amount of
     * power that they specified and have completed that invoice. On the client side,
     * that specific client would then be set into a polling loop, periodically
     * sending GET request to enquire
     *
     * @param request -  must contain user (the user id), pad (the targeted charging
     *                   pad id), and expected (the amount of power that the client
     *                   wishes to purchase.
     * @param response - returns either status code 201 or 400
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userID = request.getParameter(ARG_USER);
        String padID = request.getParameter(ARG_PAD);
        String powerExpected = request.getParameter(ARG_EXPECTED);
        double power = 0;

        try {
            power = Double.valueOf(powerExpected);
        } catch (NumberFormatException | NullPointerException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters given");
        }

        if (userID == null || padID == null || userID.equals("") || padID.equals("") || power <= 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters given");
        }

        power = Double.valueOf(powerExpected);
        DroneClient drone = DroneDB.loadDroneClient(userID);
        ChargingPad pad = DroneDB.loadChargingPad(padID);
        Transaction transaction = new Transaction(drone, pad, power);

        if (!Cache.containsActive(transaction) && !CompletedCache.containsCompleted(transaction) && transaction.begin()) {
            response.setStatus(HttpServletResponse.SC_CREATED);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "We were unable to create your transaction");
        }
    }



    /**
     * Handles the GET requests sent by the client once they are place into
     * a polling cycle. This will return JSON with one field, 'status', which
     * takes on values either 'IN_PROGRESS' or 'COMPLETE', after checking the
     * cache of active transactions.
     *
     * @param request - must contain user (the user id), pad (the targeted charging
     *                  pad id), and expected (the amount of power that the client
     *                  wishes to purchase.
     * @param response - contains JSON with status of transaction
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userID = request.getParameter(ARG_USER);
        String padID = request.getParameter(ARG_PAD);
        String powerExpected = request.getParameter(ARG_EXPECTED);
        double power = 0;

        try {
            power = Double.valueOf(powerExpected);
        } catch (NumberFormatException | NullPointerException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters given");
        }

        if (userID == null || padID == null || userID.equals("") || padID.equals("") || power <= 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters given");
        }

        power = Double.valueOf(powerExpected);
        DroneClient drone = DroneDB.loadDroneClient(userID);
        ChargingPad pad = DroneDB.loadChargingPad(padID);
        Transaction transaction = new Transaction(drone, pad, power);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Writer out = response.getWriter();
        JSONObject jsonStatus = new JSONObject();

        boolean isCompleted = CompletedCache.containsCompleted(transaction);
        boolean isActive = Cache.containsActive(transaction);
        try {
            if (isCompleted) {
                Cache.removeActive(transaction);
                jsonStatus.put(KEY_STATUS, STATUS_COMPLETE);
            } else if (isActive) {
                jsonStatus.put(KEY_STATUS, STATUS_IN_PROGRESS);
            } else {
                jsonStatus.put(KEY_STATUS, STATUS_NONEXISTENT);
            }
            jsonStatus.write(out);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        out.flush();
        out.close();
    }
}