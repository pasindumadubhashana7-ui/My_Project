
    import java.awt.Frame;
    import javax.swing.JDialog;
    import javax.swing.JOptionPane;

    public class RemoveCarDialog extends JDialog {
        private final CarController controller;
        private final Runnable onSuccessCallback;

        public RemoveCarDialog(Frame owner, CarController controller,
            Runnable onSuccessCallback) {
            super(owner, "Remove / Delete Vehicle", true);
            this.controller = controller;
            this.onSuccessCallback = onSuccessCallback;
            loadCarsForRemoval();
        }

        private void deleteSelectedCar(String carId) {
            int confirm = JOptionPane.showConfirmDialog(
                    this, "Are you sure you want to delete this car?",
                    "Confirm deletion", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = controller.removeCar(carId);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Car deleted successfully!");
                    loadCarsForRemoval();
                    if (onSuccessCallback != null) {
                        onSuccessCallback.run();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Error deleting car!",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void loadCarsForRemoval() {
            // Populate the dialog's car list here.
        }
    }