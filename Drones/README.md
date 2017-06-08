<h1>Blockchain at Berkeley's Drones Project</h1>
<body>The purpose of this project is to develop proof-of-concept and proof-of-practicality
by developing a prototype system that allows for automatic remote charging of drones.
The vision behind this project is to provide an infrastructure for drones that is
analogous to cars and gas stations. We envision a network that allows drones
flying around (such as Amazon NOW drones) to have access to independently-run charging
pads, so that drones that need charging can identify an available charging pad,
navigate to that pad, charge, and pay the pad-owner for providing the service of charging.
This project will focus on the second part: charging and payment, and is done in collaboration
with the Research and Development division of Blockchain at Berkeley (https://blockchain.berkeley.edu).
</body>


<h3>Project Overview:</h3>
<body>The Drones Project contains two main components: a centralized web server, powered by Apache Tomcat,
and the remote charging pad which contains an microcontroller with a WiFi chip programmed by us to measure and control power output.
For POC and POP purposes, we have implemented the core functionality that allows for a transaction between a
client (the drone owner) and a merchant (a charging pad owner). The flow of the system is as follows:
<ul>
  <li>Client-side application sends POST to /client, specifying their user ID, the ID of the 
  charging pad they wish to use, and the amount power they would like to purchase in the URL.</li>
  <li>ClientServlet makes sure no such transaction is already in progress, and after verifying this,
  it creates a new Transaction with the parameters specified.</li>
  <li>Server sends an invoice over email to the user via PayPal, charging them in full for the power that they
  specified they would like to purchase.</li>
  <li>The server gives some time for the client to complete the invoice, checking every couple of minutes to
  see whether the transaction has been paid for.</li>
  <li>After successful payment, the transaction is added to a Cache of active transactions, so that it may
  be processed.</li>
  <li>Client-side application would then set the client to a polling cycle, so that it would send GET
  periodically to inquire about the status of the transaction (to see whether it has been completed yet).</li>
  <li>Meanwhile, charging pads that are not actively charging a drone are on a GET poll cycle, periodically pinging
  the server to check if there are any active transactions for that specific pad that are waiting to be serviced. If
  there is an active transaction, the pad begins to charge the drone.</li>
  <li>After charging, the pad sends POST to mark the transaction as completed, the client would be notified with
  the next iteration of their poll cycle, and the transaction has been completed.</li>
</ul>


<h3>Team:</h3>
Abhinav Patel (Project Lead, server team): https://www.linkedin.com/in/abhinavrpatel/<br>
Eric Kong (server team): https://www.linkedin.com/in/erickong98/<br>
Ashwinee Panda (charging pad team):  https://www.linkedin.com/in/ashwineepanda/<br>
Aakash Parikh (charging pad team): https://www.linkedin.com/in/aakashparikh/<br>
</body>


