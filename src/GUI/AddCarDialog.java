package GUI;

import Controller.CarController;
import java.awt.*;
import java.util.UUID;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class AddCarDialog extends JDialog {
    private final JTextField brandField, modelField, priceField;
    private final JComboBox<String> statusCombo;
    private final CarController controller;
    private final Runnable onSuccessCallback;

    public AddCarDialog(Frame owner, CarController controller, Runnable onSuccessCallback) {
        super(owner, "Add New Vehicle", true);
        this.controller = controller;
        this.onSuccessCallback = onSuccessCallback;

        setSize(380, 320);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 15));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);

        formPanel.add(createLabel("Brand:"));
        brandField = new JTextField();
        formPanel.add(brandField);

        formPanel.add(createLabel("Model:"));
        modelField = new JTextField();
        formPanel.add(modelField);

        formPanel.add(createLabel("Price/Day (Rs.):"));
        priceField = new JTextField();
        formPanel.add(priceField);

        formPanel.add(createLabel("Status:"));
        statusCombo = new JComboBox<>(new String[]{"Available", "Rented", "Maintenance"});
        formPanel.add(statusCombo);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(new Color(248, 249, 250));
        JButton saveBtn = new JButton("Save Car");
        JButton cancelBtn = new JButton("Cancel");

        saveBtn.setBackground(new Color(13, 110, 253));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> saveCar());
        cancelBtn.addActionListener(e -> dispose());

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        add(formPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return lbl;
    }

    private void saveCar() {
        String brand = brandField.getText().trim();
        String model = modelField.getText().trim();
        String priceStr = priceField.getText().trim();
        String status = (String) statusCombo.getSelectedItem();

        if (brand.isEmpty() || model.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            String id = "CAR-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
            
            boolean success = controller.addCar(id, brand, model, price, status);
            if (success) {
                JOptionPane.showMessageDialog(this, "Car added successfully! ID: " + id);
                if (onSuccessCallback != null) onSuccessCallback.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add car!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Price must be a valid number!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}