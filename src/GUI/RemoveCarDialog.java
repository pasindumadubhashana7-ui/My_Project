package GUI;

import Controller.CarController;
import Project.database;
import java.awt.*;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class RemoveCarDialog extends JDialog {
    private final CarController db;
    private final Runnable onSuccessCallback;
    private JTable carTable;
    private DefaultTableModel carModel;

    public RemoveCarDialog(Frame owner, CarController carController, Runnable onSuccessCallback) {
        super(owner, "Remove / Delete Vehicle", true);
        this.db = carController;
        this.onSuccessCallback = onSuccessCallback;

        setSize(650, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel topBanner = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBanner.setBorder(new EmptyBorder(10, 15, 10, 15));
        topBanner.setBackground(new Color(255, 235, 238));
        JLabel warningLbl = new JLabel("⚠️ Select a vehicle below and click 'Delete Selected Car' to remove from database.");
        warningLbl.setForeground(new Color(183, 28, 28));
        warningLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        topBanner.add(warningLbl);

        carModel = new DefaultTableModel(new String[]{"ID", "Brand", "Model", "Price/Day (Rs.)", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        carTable = new JTable(carModel);
        carTable.setRowHeight(28);
        carTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        carTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loadCarsForRemoval();

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        JButton deleteBtn = new JButton("Delete Selected Car");
        JButton closeBtn = new JButton("Close");

        deleteBtn.setBackground(new Color(220, 53, 69));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        deleteBtn.addActionListener(e -> deleteSelectedCar());
        closeBtn.addActionListener(e -> dispose());

        bottomPanel.add(deleteBtn);
        bottomPanel.add(closeBtn);

        add(topBanner, BorderLayout.NORTH);
        add(new JScrollPane(carTable), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public RemoveCarDialog(AdminDashboard owner, database db2, Runnable onSuccessCallback2) {
        this((Frame) owner, null, onSuccessCallback2);
    }

    private void loadCarsForRemoval() {
        carModel.setRowCount(0);
        if (db == null) {
            JOptionPane.showMessageDialog(this, "Database connection is unavailable.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Object rawCars = db.getAllCars();
            if (rawCars instanceof ResultSet rs) {
                try (rs) {
                    while (rs.next()) {
                        String id = rs.getString(1);
                        String brand = rs.getString(2);
                        String model = rs.getString(3);
                        String price = rs.getString(4);
                        String status = rs.getString(5);
                        carModel.addRow(new Object[]{
                            id,
                            brand,
                            model,
                            "Rs. " + String.format("%.2f", Double.parseDouble(price)),
                            status
                        });
                    }
                }
            } else if (rawCars instanceof List<?> cars) {
                for (Object item : cars) {
                    if (item instanceof String[] car) {
                        carModel.addRow(new Object[]{
                            car[0],
                            car[1],
                            car[2],
                            "Rs. " + String.format("%.2f", Double.parseDouble(car[3])),
                            car[4]
                        });
                    }
                }
            } else if (rawCars != null) {
                JOptionPane.showMessageDialog(this, "Unexpected result type while loading cars.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading cars: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedCar() {
        if (db == null) {
            JOptionPane.showMessageDialog(this, "Database connection is unavailable.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int selectedRow = carTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a car to delete!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String carId = carModel.getValueAt(selectedRow, 0).toString();
        String brandModel = carModel.getValueAt(selectedRow, 1).toString() + " " + carModel.getValueAt(selectedRow, 2).toString();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to permanently delete car: " + brandModel + " (ID: " + carId + ")?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String idText = String.valueOf(carId);
                boolean deleted = deleteCarByReflection(idText);

                if (deleted) {
                    JOptionPane.showMessageDialog(this, "Car deleted successfully!");
                    loadCarsForRemoval();
                    if (onSuccessCallback != null) {
                        onSuccessCallback.run();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete the selected car.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting car: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean deleteCarByReflection(String carId) throws Exception {
        Method method = findDeleteMethod();
        if (method == null) {
            throw new IllegalStateException("No supported delete method found in CarController.");
        }

        Object[] args;
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length == 1 && paramTypes[0] == int.class) {
            args = new Object[]{Integer.parseInt(carId)};
        } else if (paramTypes.length == 1 && paramTypes[0] == Integer.class) {
            args = new Object[]{Integer.valueOf(carId)};
        } else {
            args = new Object[]{carId};
        }

        Object result = method.invoke(db, args);
        if (result instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(String.valueOf(result));
    }

    private Method findDeleteMethod() {
        for (String methodName : new String[]{"deleteCar", "removeCar", "deleteVehicle", "removeVehicle"}) {
            for (Method method : CarController.class.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() == 1) {
                    Class<?> paramType = method.getParameterTypes()[0];
                    if (paramType == int.class || paramType == Integer.class || paramType == String.class) {
                        return method;
                    }
                }
            }
        }
        return null;
    }
}