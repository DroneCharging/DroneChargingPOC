package blockchain.drones;

public class ChargingPad {
    private final String ID;
    private final String email;
    private final double unitPrice;

    public ChargingPad(String ID, double unitPrice, String email) {
        this.ID = ID;
        this.unitPrice = unitPrice;
        this.email = email;
    }

    public String getID() {
        return ID;
    }

    public String getEmail() {
        return this.email;
    }

    public double getUnitPrice() {
        return this.unitPrice;
    }

    @Override
    public int hashCode() {
        return Double.valueOf(unitPrice).hashCode() * ID.hashCode() + 31 * email.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !o.getClass().equals(ChargingPad.class))
            return false;

        ChargingPad c = ((ChargingPad) o);
        return ID.equals(c.ID) && unitPrice == c.unitPrice;
    }
}
