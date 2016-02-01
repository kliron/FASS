package net.neuraxis.FASS;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;


public class Seeder {
    private final Connection connection;
    private final Map<String, Integer> substances;
    private final LinkedList<Integer> substancesIds;
    private final int maxBatchSize;
    private final String insertSubstancesSQL = "INSERT INTO fass_substances (id, substance) VALUES (?,?)";
    private final String insertEffectsSQL = "INSERT INTO fass_effects (fass_substances_id, interactions, side_effects) VALUES (?,?,?)";
    private final String insertDrugsSQL = "INSERT INTO fass_drugs (id, fass_substances_id, atc, tradename, form) VALUES (?,?,?,?,?)";


    Seeder() throws ClassNotFoundException, SQLException, IOException, JAXBException, XMLStreamException {
        /* Register the postgresql driver */
        Class.forName("org.postgresql.Driver");

        final Properties properties = Main.properties;
        final Properties secretProperties = Main.secretProperties;
        final String dbUrl = secretProperties.getProperty("db.jdbc.connection.url");
        final String user = secretProperties.getProperty("db.user");
        final String password = secretProperties.getProperty("db.password");
        this.connection = DriverManager.getConnection(dbUrl, user, password);
        this.maxBatchSize = Integer.parseInt(properties.getProperty("insert.batch.size"));
        this.substances = new HashMap<>();
        this.substancesIds = new LinkedList<>();
        this.substancesIds.add(0);
    }

    /**
     * Clears all data from database tables.
     * @throws SQLException
     */
    private void clearDb() throws SQLException {
        try (final Statement stmt = connection.createStatement()){
            final List<String> tables = new ArrayList<>(Arrays.asList("fass_drugs", "fass_effects", "fass_substances"));
            for (final String t : tables) {
                final String delete = "DELETE FROM " + t;
                stmt.execute(delete);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    private void executeBatch(final List<Drug> drugs) throws SQLException {
        final PreparedStatement insertSubstances = connection.prepareStatement(insertSubstancesSQL);
        final PreparedStatement insertEffects = connection.prepareStatement(insertEffectsSQL);
        final PreparedStatement insertDrugs = connection.prepareStatement(insertDrugsSQL);
        for (final Drug drug : drugs) {
            Integer substances_id = substances.get(drug.getSubstance());
            if (substances_id == null) {
                substances_id = (substancesIds.getLast() + 1);
                insertSubstances.setInt(1, substances_id);
                insertSubstances.setString(2, drug.getSubstance());
                insertSubstances.addBatch();

                insertEffects.setInt(1, substances_id);
                insertEffects.setString(2, drug.getInteractions());
                insertEffects.setString(3, drug.getSideEffects());
                insertEffects.addBatch();

                substances.put(drug.getSubstance(), substances_id);
                substancesIds.add(substances_id);
            }
            insertDrugs.setString(1, drug.getDrugId());
            insertDrugs.setInt(2, substances_id);
            insertDrugs.setString(3, drug.getAtc());
            insertDrugs.setString(4, drug.getTradeName());
            insertDrugs.setString(5, drug.getForm());
            insertDrugs.addBatch();
        }
        insertSubstances.executeBatch();
        insertEffects.executeBatch();
        insertDrugs.executeBatch();
    }


    /**
     * Seeds data read from a single file.
     * @param file absolute Path to data file.
     * @return number of seeded drugs.
     */
    private long seedFromFile(final Path file) throws XMLStreamException, SQLException {
        long count = 0;
        System.out.println("Processing " + file.toAbsolutePath().toString());
        try (final StreamXMLUnmarshaller<Drug> unmarshaller = new StreamXMLUnmarshaller<>(Drug.class)){
            unmarshaller.open(file);

            final List<Drug> drugs = new ArrayList<>();
            while(true) {
                if (unmarshaller.hasNext()) {
                    if (drugs.size() >= maxBatchSize) {
                        executeBatch(drugs);
                        drugs.clear();
                    }
                    drugs.add(unmarshaller.read());
                    count++;
                } else {
                    // Last run
                    if (drugs.size() > 0) {
                        executeBatch(drugs);
                    }
                    break;
                }
            }
        } catch(XMLStreamException | IOException | JAXBException ex) {
            ex.printStackTrace();
        }
        return count;
    }


    public void seedDrugs() throws JAXBException, XMLStreamException, IOException, SQLException {
        final long startT = System.currentTimeMillis();
        System.out.println("Clearing database tables");
        clearDb();

        System.out.println("Reading data.");
        long count = seedFromFile(Main.drugsXml);

        final long endT = System.currentTimeMillis();
        System.out.printf("Seeding completed in %d ms, seeded %d entries.\n", endT - startT, count);
    }
}
