package ca.learncodegap.hospital;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class PatientDAO {
    public List<Patient> find(String search) throws SQLException {
        boolean filtered = search != null && !search.isBlank();
        String sql = "SELECT patient_id, name, age, gender, phone, diagnosis, admission_date FROM patients" +
                (filtered ? " WHERE name LIKE ? OR phone LIKE ?" : "") + " ORDER BY patient_id DESC";
        List<Patient> rows = new ArrayList<>();
        try (Connection c = DatabaseConnection.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (filtered) { String q = "%" + search.trim() + "%"; ps.setString(1,q); ps.setString(2,q); }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(new Patient(rs.getInt("patient_id"),rs.getString("name"),rs.getInt("age"),rs.getString("gender"),rs.getString("phone"),rs.getString("diagnosis"),rs.getDate("admission_date").toLocalDate()));
            }
        }
        return rows;
    }
    public void add(Patient p) throws SQLException {
        String sql="INSERT INTO patients(name,age,gender,phone,diagnosis,admission_date) VALUES (?,?,?,?,?,?)";
        try(Connection c=DatabaseConnection.open();PreparedStatement ps=c.prepareStatement(sql)) { bind(ps,p);ps.executeUpdate(); }
    }
    public void update(Patient p) throws SQLException {
        String sql="UPDATE patients SET name=?,age=?,gender=?,phone=?,diagnosis=?,admission_date=? WHERE patient_id=?";
        try(Connection c=DatabaseConnection.open();PreparedStatement ps=c.prepareStatement(sql)) {
            bind(ps,p);ps.setInt(7,p.id());if(ps.executeUpdate()==0)throw new SQLException("Patient no longer exists.");
        }
    }
    public void delete(int id) throws SQLException {
        try(Connection c=DatabaseConnection.open();PreparedStatement ps=c.prepareStatement("DELETE FROM patients WHERE patient_id=?")) {
            ps.setInt(1,id);if(ps.executeUpdate()==0)throw new SQLException("Patient no longer exists.");
        }
    }
    private void bind(PreparedStatement ps,Patient p) throws SQLException {
        ps.setString(1,p.name());ps.setInt(2,p.age());ps.setString(3,p.gender());ps.setString(4,p.phone());ps.setString(5,p.diagnosis());ps.setDate(6,Date.valueOf(p.admissionDate()));
    }
}
