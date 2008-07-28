package org.bdwyer.rabinhash;

import java.math.BigInteger;

public class RabinFingerPrint {

	private static final BigInteger X = BigInteger.valueOf( 2L );

	private final BigInteger poly;
	private final BigInteger poly_xor;
	private  BigInteger removalOperand;
	private final int degree;
	private final int windowSize;
	
	
	public RabinFingerPrint(BigInteger poly, int degree, int windowSize) {
		super();
		this.poly = poly;
		this.poly_xor = poly.clearBit(degree);		
		this.removalOperand = X.modPow(BigInteger.valueOf(windowSize - 1), poly);
		this.removalOperand = removalOperand.negate();
		this.degree = degree;
		this.windowSize = windowSize;
		


		System.out.println( "poly: " + poly.toString(16).toUpperCase() );
		System.out.println( "poly_xor: " + poly_xor.toString(16).toUpperCase() );
		System.out.println( "removal: " + removalOperand.toString(16).toUpperCase() );
	}

	public BigInteger fingerprintBytes( byte[] bytes ){
		BigInteger f = BigInteger.ZERO;
		for( byte b : bytes ){
			f = appendByte( f, b );
		}
		return f;
	}
	
	public BigInteger appendByte(BigInteger f0, byte b) {
		BigInteger f = f0;
		for (int i = 7; i >= 0; i--) {
			boolean inboundBit = (((b >> i) & 1) == 1);
			f = shiftInBit(f, inboundBit);
		}
		return f;
	}
	
	private BigInteger shiftInBit(BigInteger f, boolean inboundBit) {
		// get output bit
		boolean r1 = f.testBit(degree);
		// shift
		f = f.shiftLeft(1);
		// set input bit
		if (inboundBit) f = f.setBit(0);
		// xor on output == 1
		if (r1) f = f.xor(poly_xor);
		return f.mod(poly);
	}
	
	private BigInteger shiftOutBit(BigInteger f, boolean outboundBit) {
		if (outboundBit){
			// subtract b[1]*x^n. in this case we assume n == degree;
			f = f.xor( removalOperand ).mod(poly);
		}
		return f;
	}
	
	public BigInteger shiftOutShiftIn(BigInteger f0, byte bOut, byte bIn) {
		BigInteger f = f0;

		for (int i = 7; i >= 0; i--) {
			// shift out bit
			boolean outboundBit = (((bOut >> i) & 1) == 1);
			f = shiftOutBit(f, outboundBit);

			// shift in bit
			boolean inboundBit = (((bIn >> i) & 1) == 1);
			f = shiftInBit(f, inboundBit);
		}
		return f;
	}
	
	
	private static byte[] bytes = new byte[]{
			0x77, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
			0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17,
			0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27,
			0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37,
	};
	
	public static void main(String[] args) {
		BigInteger poly = new BigInteger("804AE1202C306041", 16);

		int windowBytes = 8;
		int windowSize = windowBytes * 8;
		RabinFingerPrint rabin = new RabinFingerPrint( poly, 63, windowSize );
		
		// read in first 8 bytes
		BigInteger f0 = BigInteger.ZERO;
		int i = 0;
		for( ; i < windowBytes; i++ ){
			f0 = rabin.appendByte( f0, bytes[i] );
		}
		
		// shift in out next byte
		for( ; i < windowBytes*2; i++ ){
			f0 = rabin.shiftOutShiftIn(f0, bytes[i - windowBytes], bytes[i]);	
		}
		
		// just fingerprint window
		BigInteger f1 = BigInteger.ZERO;
		for( i = windowBytes; i < windowBytes*2; i++ ){
			f1 = rabin.appendByte( f1, bytes[i]);
		}

		System.out.println("f0: " + f0.toString(16).toUpperCase());
		System.out.println("f1: " + f1.toString(16).toUpperCase());
		//System.out.println("f2: " + f2.toString(16).toUpperCase());
	}
}
