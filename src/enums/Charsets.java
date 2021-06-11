package enums;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public enum Charsets {
	ISO_8859_1 {
		public Charset getCharset() {
			return StandardCharsets.ISO_8859_1;
		}
	},
	
	US_ASCII {
		public Charset getCharset() {
			return StandardCharsets.US_ASCII;
		}
	},
	
	UTF_8 {
		public Charset getCharset() {
			return StandardCharsets.UTF_8;
		}
	},
	
	UTF_16 {
		public Charset getCharset() {
			return StandardCharsets.UTF_16;
		}
	}
	,
	UTF_16BE {
		public Charset getCharset() {
			return StandardCharsets.UTF_16BE;
		}
	},
	UTF_16LE {
		public Charset getCharset() {
			return StandardCharsets.UTF_16LE;
		}
	};
	
	@Override
	public String toString() {
		switch (this) {
		case ISO_8859_1:
			return StandardCharsets.ISO_8859_1.displayName();
		case US_ASCII:
			return StandardCharsets.US_ASCII.displayName();
		case UTF_8:
			return StandardCharsets.UTF_8.displayName();
		case UTF_16:
			return StandardCharsets.UTF_16.displayName();
		case UTF_16BE:
			return StandardCharsets.UTF_16BE.displayName();
		case UTF_16LE:
			return StandardCharsets.UTF_16LE.displayName();
		default:throw new IllegalArgumentException();
		}
	}
}