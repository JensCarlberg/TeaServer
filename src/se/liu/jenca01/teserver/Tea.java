package se.liu.jenca01.teserver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

public class Tea {

	public static class Builder {
        private String tea;
        private String type;
        private String pot;
        private Double volume;
        private Date start;
        private Long id;

        public Builder fromRequest(HttpServletRequest request) {
    		String tea = request.getParameter("tea");
    		String type = request.getParameter("type");
    		String pot = request.getParameter("pot");
    		String volume = request.getParameter("volume");
    		String start = request.getParameter("start");
    		String id = request.getParameter("id");

    		if (tea != null) tea(tea);
    		if (type != null) type(type);
    		if (pot != null) pot(pot);
    		if (volume != null) volume(Double.parseDouble(volume));
    		if (start != null) start(Long.parseLong(start));
    		if (id != null) id(Long.parseLong(id));

			return this;
		}

        public Builder tea(String tea) { this.tea = tea; return this; }
        public Builder type(String teaType) { type = teaType; return this; }
        public Builder pot(String pot) { this.pot = pot; return this; }
        public Builder volume(double volume) { this.volume = volume; return this; }
        public Builder start(long start) { this.start = new Date(start); return this; }
        public Builder id(long id) { this.id = id; return this; }

        public Tea build() throws InstantiationException {
        	if (tea == null) throw new InstantiationException("Missing tea");
        	if (type == null) type = "";
        	if (pot == null) pot = "";
        	if (volume == null) volume = 3.0;
        	if (start == null) start = new Date();
        	if (id == null) id = start.getTime();

        	return new Tea(tea, type, pot, volume, start, id);
        }
	}

	public final String name;
	public final String type;
	public final String pot;
	public final double volume;
	public final Date start;
	public final long id;

	public Tea(String tea, String type, String pot, double volume, Date start, long id) {
		name = tea;
		this.type = type;
		this.pot = pot;
		this.volume = volume;
		this.start = start;
		this.id = id;
	}

	@Override
	public String toString() {
        return String.format("%1$tF %1$tT\t%2$s\t%3$s\t%4$s\t%5$s\t%6$s",
        		start, name, type, volume, pot, id);
	}

	public static Tea parse(String line) {
		String[] parts = line.split("\t");
		try {
			return new Tea(
					parts[1],
					parts[2],
					parts[4],
					Double.parseDouble(parts[3]),
					parseDate(parts[0]),
					Long.parseLong(parts[5])
					);
		} catch (Exception e) {
			System.err.println(String.format("Could not parse tea from '%s'", line));
			e.printStackTrace();
			return null;
		}
	}

	private static Date parseDate(String date) throws ParseException {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
	}
}
