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
 * Servlet that handles requests from the charging pad. Unless a pad is in
 * an active transaction, it is on a poll loop. It will continually send
 * GET requests every period of time, to check for an active pending
 * transaction. If the result of the GET request is that there is an active
 * transaction ready to be serviced, the charging pad charges the drone.
 * After charging, the pad will send POST to indicate that the charging
 * has been completed, which is when the server will clear that corresponding
 * transaction of the list of active transactions.
 */
@WebServlet(name = "DeviceServlet")
public class DeviceServlet extends HttpServlet {
    private static final String ARG_USER = "user";
    private static final String ARG_PAD = "pad";
    private static final String ARG_EXPECTED = "expected";


    /**
     * Handles the POST request sent to this server by the charging pad. If the
     * correct parameters are given, then the transaction is cleared from the list
     * of active transactions and completed. If cleared successfully, then HTTP "OK"
     * code (200) is written back to the user. Else, HTTP "bad request" code (400)
     * is written back.
     *
     * @param request -  must contain user (the user id), pad (the charging pad's id),
     *                   and expected (the amount of power that the client purchased).
     * @param response - returns either status code 200 or 400
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userID = request.getParameter(ARG_USER);
        String padID = request.getParameter(ARG_PAD);
        String powerExpected = request.getParameter(ARG_EXPECTED);
        double power = 0;

        System.out.println("/device POST FROM pad: " + padID + " |-----ARGS ARE user: " + userID + " expected: " + powerExpected);
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

        DroneClient drone = DroneDB.loadDroneClient(userID);
        ChargingPad pad = DroneDB.loadChargingPad(padID);
        Transaction transaction = new Transaction(drone, pad, power);
        transaction.complete();
    }


    /**
     * Handles the GET requests sent to this servlet by the polling charging pads.
     * The server performs lookups to see if there is an active transaction. If it
     * does not find one, it returns a JSON object with field 'has_transaction' set to
     * 'FALSE'. Else, it returns a JSON object with field 'has_transaction' set to
     * 'TRUE', 'expected_power' set to the correct expected power, 'pad' set to the
     * pad ID of the sender, and 'user' set to the user ID of the client.
     *
     * @param request - must contain pad (the ID of the charging pad that is the sender)
     * @param response - contains JSON with specified parameters
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String padID = request.getParameter(ARG_PAD);
        System.out.println("/device GET FROM pad: " + padID);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Writer out = response.getWriter();
        JSONObject jsonStatus = new JSONObject();

        boolean isActive = Cache.containsActive(padID);
        try {
            if(isActive) {
                Transaction t = Cache.getTransaction(padID);
                jsonStatus.put("has_transaction", true);
                jsonStatus.put("expected_power", t.getPowerExpected());
                jsonStatus.put("pad", t.getPad().getID());
                jsonStatus.put("user", t.getClient().getID());
            } else {
                jsonStatus.put("has_transaction", false);
            }
            jsonStatus.write(out);
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            out.flush();
            out.close();
        }
    }
}

