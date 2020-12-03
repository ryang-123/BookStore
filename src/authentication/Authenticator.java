package authentication;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class Authenticator {
	DataSource ds;

	public Authenticator() {
		try {
			this.ds = (DataSource) (new InitialContext()).lookup("java:/comp/env/jdbc/EECS");
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	public byte[] cypherPassword(String password) throws NoSuchAlgorithmException {
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[16];
		random.nextBytes(salt);
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		md.update(salt);
		byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));

		return hashedPassword;
	}

	public boolean authenticate(String username, String password) throws NoSuchAlgorithmException, SQLException {
		boolean authenticate = false;
		byte[] hashedPassword = this.cypherPassword(password);
		String query = ("select password from users where username = '" + username + "'");
		Connection con = (this.ds).getConnection();
		PreparedStatement p = con.prepareStatement(query);
		ResultSet r = p.executeQuery();
		String storedPass = "";
		while (r.next()) {
			storedPass = r.getString("password");
		}
		if (hashedPassword.toString().contentEquals(storedPass)) {
			authenticate = true;
		}
		r.close();
		p.close();
		con.close();

		return authenticate;

	}
	public int registerUser (String fname, String lname, String username, String email, String password) throws SQLException, NoSuchAlgorithmException {
		byte[] hashedPassword = this.cypherPassword(password);
		String hashedP = hashedPassword.toString();
		String query = ("INSERT INTO USERS values(?, ?, ?, ?, ?)");
		Connection con = (this.ds).getConnection();
		PreparedStatement p = con.prepareStatement(query);
		p.setString(1, fname);
		p.setString(2, lname);
		p.setString(3, username);
		p.setString(4, hashedP);
		p.setString(5, email);
		p.executeUpdate();
		p.close();
		con.close();
		return p.executeUpdate();

	}
	public boolean userExists(String username) throws SQLException {
		boolean exists = false;
		String query = ("select * from users where username = '" + username + "'");
		Connection con = (this.ds).getConnection();
		PreparedStatement p = con.prepareStatement(query);
		ResultSet r = p.executeQuery();
		int count = 0;
		while (r.next()) {
			count += 1;
		}
		if (count >= 1) {
			exists = true;
		}
		r.close();
		p.close();
		con.close();

		return exists;
	}
}
