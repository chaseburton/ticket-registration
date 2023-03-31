public class Seat {
	public String row;
	public int seat;
	public int price;
	public String customerName;
	public String customerPhone;

	public Seat(String row, int seat, int price) {
		this.row = row;
		this.seat = seat;
		this.price = price;
	}

	public Seat(String row, int seat, int price, String customerName, String customerPhone) {
		this.row = row;
		this.seat = seat;
		this.price = price;
		this.customerName = customerName;
		this.customerPhone = customerPhone;
	}

	public Seat() {
	
	}
}
