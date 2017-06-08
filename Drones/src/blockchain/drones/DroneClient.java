package blockchain.drones;

public class DroneClient {
    private final String ID;
    private final String email;

    public DroneClient(String ID, String email) {
        this.ID = ID;
        this.email = email;
    }

    protected String getID() {
        return ID;
    }


    protected String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !o.getClass().equals(DroneClient.class))
            return false;

        DroneClient d = ((DroneClient) o);
        return ID.equals(d.ID) && email.equals(d.email);
    }

    @Override
    public int hashCode() {
        return ID.hashCode() + 31 * email.hashCode();
    }
}
