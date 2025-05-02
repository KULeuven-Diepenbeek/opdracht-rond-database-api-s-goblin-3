package be.kuleuven;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SpelerRepositoryJDBCimpl implements SpelerRepository {
  private Connection connection;

  // Constructor
  SpelerRepositoryJDBCimpl(Connection connection) {
    // TODO: vul contructor verder aan
    this.connection=connection;
  }

  @Override
  public void addSpelerToDb(Speler speler) {
    // TODO: verwijder de "throw new UnsupportedOperationException" en schrijf de code die de gewenste methode op de juiste manier implementeerd zodat de testen slagen.
    String sql = "INSERT INTO speler (tennisvlaanderenid, naam, punten) VALUES (?, ?, ?)";//int tennisvlaanderenId, String naam, int punten
try {
  PreparedStatement preparedStatement = connection.prepareStatement(sql);
  preparedStatement.setInt(1, speler.getTennisvlaanderenid());
  preparedStatement.setString(2, speler.getNaam());
  preparedStatement.setInt(3, speler.getPunten());
  preparedStatement.executeUpdate();
  preparedStatement.close();

  connection.commit();
}   catch (SQLException e) {
    if (e.getMessage().contains("PRIMARY KEY")) {
      throw new RuntimeException(" A PRIMARY KEY constraint failed", e);
    } else {
      throw new RuntimeException("Error inserting speler into DB", e);
    }
  }


    //throw new UnsupportedOperationException("Unimplemented method 'addSpelerToDb'");
  }

  @Override
  public Speler getSpelerByTennisvlaanderenId(int tennisvlaanderenId) {
    // TODO: verwijder de "throw new UnsupportedOperationException" en schrijf de code die de gewenste methode op de juiste manier implementeerd zodat de testen slagen.
    String sql = "SELECT * FROM speler WHERE tennisvlaanderenid = ?";
    try {
      PreparedStatement preparedStatement =  connection.prepareStatement(sql);
      preparedStatement.setInt(1,tennisvlaanderenId);
      ResultSet resultSet = preparedStatement.executeQuery();
      if (resultSet.next()){
        int id = resultSet.getInt("tennisvlaanderenid");
        String naam = resultSet.getString("naam");
        int punten = resultSet.getInt("punten");
        return new Speler(id,naam,punten);
      }else {
        throw new InvalidSpelerException(""+tennisvlaanderenId);
      }


    } catch (SQLException e) {
      throw new RuntimeException(e);
    }


   // throw new UnsupportedOperationException("Unimplemented method 'getSpelerByTennisvlaanderenId'");
  }

  @Override
  public List<Speler> getAllSpelers() {
    // TODO: verwijder de "throw new UnsupportedOperationException" en schrijf de code die de gewenste methode op de juiste manier implementeerd zodat de testen slagen.

    String sql = "SELECT * FROM speler";
    try {
      PreparedStatement preparedStatement =  connection.prepareStatement(sql);
      ArrayList<Speler> spelers= new ArrayList<>();

      ResultSet resultSet = preparedStatement.executeQuery();
      while (resultSet.next()){
        int id = resultSet.getInt("tennisvlaanderenid");
        String naam = resultSet.getString("naam");
        int punten = resultSet.getInt("punten");
        spelers.add( new Speler(id,naam,punten));
      }return spelers;


    } catch (SQLException e) {
      throw new RuntimeException(e);
    }


    //throw new UnsupportedOperationException("Unimplemented method 'getAllSpelers'");
  }

  @Override
  public void updateSpelerInDb(Speler speler) {
    // TODO: verwijder de "throw new UnsupportedOperationException" en schrijf de code die de gewenste methode op de juiste manier implementeerd zodat de testen slagen.
    String sql = "UPDATE speler SET naam = ?, punten = ? WHERE tennisvlaanderenid = ?";
    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setString(1, speler.getNaam());
      preparedStatement.setInt(2, speler.getPunten());
      preparedStatement.setInt(3, speler.getTennisvlaanderenid());

      int updatedRows = preparedStatement.executeUpdate();
      if (updatedRows == 0) {
        throw new RuntimeException("Invalid Speler met identification: " + speler.getTennisvlaanderenid());
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error updating speler in database", e);
    }


    //throw new UnsupportedOperationException("Unimplemented method 'updateSpelerInDb'");
  }

  @Override
  public void deleteSpelerInDb(int tennisvlaanderenid) {
    // TODO: verwijder de "throw new UnsupportedOperationException" en schrijf de code die de gewenste methode op de juiste manier implementeerd zodat de testen slagen.
    String sql = "DELETE FROM speler WHERE tennisvlaanderenid = ?";
    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

      preparedStatement.setInt(1, tennisvlaanderenid);

      int updatedRows = preparedStatement.executeUpdate();
      if (updatedRows == 0){
        throw new RuntimeException("Invalid Speler met identification: "+tennisvlaanderenid);
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error updating speler in database", e);
    }




    //throw new UnsupportedOperationException("Unimplemented method 'deleteSpelerInDb'");
  }

  @Override
  public String getHoogsteRankingVanSpeler(int tennisvlaanderenid) {
    // TODO: verwijder de "throw new UnsupportedOperationException" en schrijf de code die de gewenste methode op de juiste manier implementeerd zodat de testen slagen.
    String sql = """
    SELECT w.finale, w.winnaar, t.clubnaam
    FROM wedstrijd w
    JOIN tornooi t ON w.tornooi = t.id
    WHERE w.speler1 = ? OR w.speler2 = ?
    """;
    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setInt(1, tennisvlaanderenid);
      preparedStatement.setInt(2, tennisvlaanderenid);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        String result =null;

        while (resultSet.next()) {
          int finale = resultSet.getInt("finale");
          int winnaar = resultSet.getInt("winnaar");
          String clubnaam = resultSet.getString("clubnaam");

          if (finale == 1 && winnaar == tennisvlaanderenid) {
            return "Hoogst geplaatst in het tornooi van " + clubnaam + " met plaats in de winst";
          } else if (finale == 1) {
            result = "Hoogst geplaatst in het tornooi van " + clubnaam + " met plaats in de finale";
          } else if (finale == 2 && result == null) {
            result = "Hoogst geplaatst in het tornooi van " + clubnaam + " met plaats in de halve finale";
          } else if (result == null) {
            result = "Hoogst geplaatst in het tornooi van " + clubnaam + " met plaats in de kwartfinale";
          }
        }

        return result; // will be null if player played no matches


      }
    } catch (SQLException e) {
      throw new RuntimeException("Error retrieving ranking", e);
    }
    //throw new UnsupportedOperationException("Unimplemented method 'getHoogsteRankingVanSpeler'");
  }

  @Override
  public void addSpelerToTornooi(int tornooiId, int tennisvlaanderenId) {
    // TODO: verwijder de "throw new UnsupportedOperationException" en schrijf de code die de gewenste methode op de juiste manier implementeerd zodat de testen slagen.
    String sql = "INSERT INTO speler_speelt_tornooi (speler, tornooi) VALUES (?, ?)";//i dont know why but java cant seem to find the table

    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setInt(1, tennisvlaanderenId);
      preparedStatement.setInt(2, tornooiId);

      int insertedRows = preparedStatement.executeUpdate();
      if (insertedRows == 0) {
        throw new RuntimeException("Failed to add speler to tornooi.");
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error inserting speler into tornooi", e);
    }

    //throw new UnsupportedOperationException("Unimplemented method 'addSpelerToTornooi'");
  }

  @Override
  public void removeSpelerFromTornooi(int tornooiId, int tennisvlaanderenId) {
    // TODO: verwijder de "throw new UnsupportedOperationException" en schrijf de code die de gewenste methode op de juiste manier implementeerd zodat de testen slagen.
    String sql = "DELETE FROM speler_speelt_tornooi WHERE speler = ? AND tornooi = ?";

    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setInt(1, tennisvlaanderenId);
      preparedStatement.setInt(2, tornooiId);

      int affectedRows = preparedStatement.executeUpdate();
      if (affectedRows == 0) {
        throw new RuntimeException("No such speler-tornooi link found to remove.");
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error removing speler from tornooi", e);
    }
    //throw new UnsupportedOperationException("Unimplemented method 'removeSpelerFromTornooi'");
  }
}
