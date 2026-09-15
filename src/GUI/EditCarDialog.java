package GUI;

import Controller.CarController;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class EditCarDialog extends JDialog {
    private final JTextField idField, brandField, modelField, priceField;
    private final JComboBox<String> statusCombo;
    private final CarController controller;
    private final Runnable onSuccessCallback;

    public EditCarDialog(Frame owner, CarController controller, String carId, String brand, String model, double price, String status, Runnable onSuccessCallback) {
        super(owner, "Edit Vehicle Details", true);
        this.controller = controller;
        this.onSuccessCallback = onSuccessCallback;

        setSize(380, 360);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 15));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);

        formPanel.add(createLabel("ID (Read-only):"));
        idField = new JTextField(carId);
        idField.setEditable(false);
        idField.setBackground(new Color(233, 236, 239));
        formPanel.add(idField);

        formPanel.add(createLabel("Brand:"));
        brandField = new JTextField(brand);
        formPanel.add(brandField);

        formPanel.add(createLabel("Model:"));
        modelField = new JTextField(model);
        formPanel.add(modelField);

        formPanel.add(createLabel("Price/Day (Rs.):"));
        priceField = new JTextField(String.format("%.2f", price));
        formPanel.add(priceField);

        formPanel.add(createLabel("Status:"));
        statusCombo = new JComboBox<>(new String[]{"Available", "Rented", "Maintenance"});
        statusCombo.setSelectedItem(status);
        formPanel.add(statusCombo);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(new Color(248, 249, 250));
        JButton updateBtn = new JButton("Update Car");
        JButton cancelBtn = new JButton("Cancel");

        updateBtn.setBackground(new Color(255, 193, 7));
        updateBtn.setForeground(Color.BLACK);
        updateBtn.setFocusPainted(false);
        
        updateBtn.addActionListener(e -> updateCar());
        cancelBtn.addActionListener(e -> dispose());

        btnPanel.add(cancelBtn);
        btnPanel.add(updateBtn);

        add(formPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return lbl;
    }

    private void updateCar() {
        String id = idField.getText().trim();
        String brand = brandField.getText().trim();
        String model = modelField.getText().trim();
        String priceStr = priceField.getText().trim();
        String status = (String) statusCombo.getSelectedItem();

        if (brand.isEmpty() || model.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double price = Double.parseDouble(priceStr.replaceAll("[^0-9.]", ""));
            if (controller == null) {
                System.err.println("DEBUG: Controller is NULL inside EditCarDialog!");
                return;
            }
            boolean success = controller.updateCar(id, brand, model, price, status);
            if (success) {
                JOptionPane.showMessageDialog(this, "Car updated successfully!");
                if (onSuccessCallback != null) onSuccessCallback.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update car!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Price must be a valid number!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}