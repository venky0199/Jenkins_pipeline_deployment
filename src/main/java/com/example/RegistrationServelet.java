package com.example;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.*;
import org.json.JSONObject;

@WebServlet("/register")
public class RegistrationServlet extends HttpServlet {

    // Database configuration
    private static final String DB_URL = "jdbc:mysql://ec2-54-90-244-153.compute-1.amazonaws.com:3306/Register";
    private static final String DB_USER = "venky";
    private static final String DB_PASSWORD = "Venky@123";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JSONObject jsonResponse = new JSONObject();

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String mobile = request.getParameter("mobile");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                
                // Generate custom ID
                String prefix = "16ST25";
                String newCustomId = generateCustomId(conn, prefix);

                // Insert new user
                String sql = "INSERT INTO Register (custom_id, username, password, mobile) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, newCustomId);
                    pstmt.setString(2, username);
                    pstmt.setString(3, password);
                    pstmt.setString(4, mobile);
                    pstmt.executeUpdate();

                    jsonResponse.put("status", "success");
                    jsonResponse.put("customId", newCustomId);
                }
            }
        } catch (Exception e) {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", e.getMessage());
        }

        response.getWriter().write(jsonResponse.toString());
    }

    private String generateCustomId(Connection conn, String prefix) throws SQLException {
        String getLastIdSql = "SELECT custom_id FROM Register ORDER BY id DESC LIMIT 1";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(getLastIdSql)) {
            
            int nextNum = 1;
            if (rs.next()) {
                String lastId = rs.getString("custom_id");
                nextNum = Integer.parseInt(lastId.substring(6)) + 1;
            }
            return prefix + String.format("%05d", nextNum);
        }
    }
}
