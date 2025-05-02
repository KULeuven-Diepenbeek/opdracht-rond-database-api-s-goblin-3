package be.kuleuven;


import java.sql.Connection;
import java.util.List;
import java.util.Map;


import org.jdbi.v3.core.Jdbi;

public class SpelerRepositoryJDBIimpl implements SpelerRepository {
  private final Jdbi jdbi;

  // Constructor
  SpelerRepositoryJDBIimpl(Connection connection) {
    // TODO: vul verder aan of verbeter

      this.jdbi = Jdbi.create(connection);

  }


  @Override
  public void addSpelerToDb(Speler speler) {
    // TODO: verwijder de "throw new UnsupportedOperationException" en schrijf de code die de gewenste methode op de juiste manier implementeerd zodat de testen slagen.
    String sql = "INSERT INTO speler (tennisvlaanderenid, naam, punten) VALUES (:id, :naam, :punten)";
    try {
      jdbi.useHandle(handle ->
              handle.createUpdate(sql)
                      .bind("id", speler.getTennisvlaanderenid())
                      .bind("naam", speler.getNaam())
                      .bind("punten", speler.getPunten())
                      .execute()
      );
    } catch (Exception e) {
      if (e.getMessage().contains("PRIMARY KEY")) {
        throw new RuntimeException(" A PRIMARY KEY constraint failed", e);
      }
      throw new RuntimeException("Error inserting speler into DB", e);
    }
   // throw new UnsupportedOperationException("Unimplemented method 'addSpelerToDb'");
  }

  @Override
  public Speler getSpelerByTennisvlaanderenId(int tennisvlaanderenId) {
    // TODO: verwijder de "throw new UnsupportedOperationException" en schrijf de code die de gewenste methode op de juiste manier implementeerd zodat de testen slagen.
    String sql = "SELECT tennisvlaanderenid, naam, punten FROM speler WHERE tennisvlaanderenid = :id";
    try {
      return jdbi.withHandle(handle ->
              handle.createQuery(sql)
                      .bind("id", tennisvlaanderenId)
                      .map((rs, ctx) -> new Speler(
                              rs.getInt("tennisvlaanderenid"),
                              rs.getString("naam"),
                              rs.getInt("punten")
                      ))
                      .findOne()
                      .orElseThrow(() -> new RuntimeException("Speler not found"))
      );
    } catch (Exception e) {
      throw new RuntimeException("Invalid Speler met identification: "+tennisvlaanderenId);
    }
   // throw new UnsupportedOperationException("Unimplemented method 'getSpelerByTennisvlaanderenId'");
  }

  @Override
  public List<Speler> getAllSpelers() {
    // TODO: verwijder de "throw new UnsupportedOperationException" en schrijf de code die de gewenste methode op de juiste manier implementeerd zodat de testen slagen.
    String sql = "SELECT tennisvlaanderenid, naam, punten FROM speler";
    try {
      return jdbi.withHandle(handle ->
              handle.createQuery(sql)
                      .map((rs, ctx) -> new Speler(
                              rs.getInt("tennisvlaanderenid"),
                              rs.getString("naam"),
                              rs.getInt("punten")
                      ))
                      .list()
      );
    } catch (Exception e) {
      throw new RuntimeException("Error retrieving all spelers from DB", e);
    }
    //throw new UnsupportedOperationException("Unimplemented method 'getAllSpelers'");
  }

  @Override
  public void updateSpelerInDb(Speler speler) {
    // TODO: verwijder de "throw new UnsupportedOperationException" en schrijf de code die de gewenste methode op de juiste manier implementeerd zodat de testen slagen.
    String sql = """
        UPDATE speler
        SET naam = :naam,
            punten = :punten
        WHERE tennisvlaanderenid = :id
        """;

    try {
      int rowsAffected = jdbi.withHandle(handle ->
              handle.createUpdate(sql)
                      .bind("naam", speler.getNaam())
                      .bind("punten", speler.getPunten())
                      .bind("id", speler.getTennisvlaanderenid())
                      .execute()
      );

      if (rowsAffected == 0) {
        throw new RuntimeException("Invalid Speler met identification: " + speler.getTennisvlaanderenid());
      }

    } catch (Exception e) {
      throw new RuntimeException("Invalid Speler met identification: " + speler.getTennisvlaanderenid());
    }
    //throw new UnsupportedOperationException("Unimplemented method 'updateSpelerInDb'");
  }

  @Override
  public void deleteSpelerInDb(int tennisvlaanderenid) {
    // TODO: verwijder de "throw new UnsupportedOperationException" en schrijf de code die de gewenste methode op de juiste manier implementeerd zodat de testen slagen.
    String sql = "DELETE FROM speler WHERE tennisvlaanderenid = :id";
    try {
      int rowsDeleted = jdbi.withHandle(handle ->
              handle.createUpdate(sql)
                      .bind("id", tennisvlaanderenid)
                      .execute()
      );

      if (rowsDeleted == 0) {
        throw new RuntimeException("Invalid Speler met identification: " + tennisvlaanderenid);
      }
    } catch (Exception e) {
      throw new RuntimeException("Invalid Speler met identification: " + tennisvlaanderenid);
    }
   // throw new UnsupportedOperationException("Unimplemented method 'deleteSpelerInDb'");
  }

  @Override
  public String getHoogsteRankingVanSpeler(int tennisvlaanderenid) {
    // TODO: verwijder de "throw new UnsupportedOperationException" en schrijf de code die de gewenste methode op de juiste manier implementeerd zodat de testen slagen.
    String sql = """
        SELECT w.finale, w.winnaar, t.clubnaam
        FROM wedstrijd w
        JOIN tornooi t ON w.tornooi = t.id
        WHERE w.speler1 = :id OR w.speler2 = :id
        """;

    try {
      return jdbi.withHandle(handle -> {
        List<Map<String, Object>> results = handle.createQuery(sql)
                .bind("id", tennisvlaanderenid)
                .mapToMap()
                .list();

        int bestRank = Integer.MAX_VALUE;
        String bestClub = null;
        String bestLabel = null;

        for (Map<String, Object> row : results) {
          int finale = (int) row.get("finale");
          int winnaar = row.get("winnaar") != null ? (int) row.get("winnaar") : -1;
          String clubnaam = (String) row.get("clubnaam");

          int rank;
          String label;

          if (finale == 1 && winnaar == tennisvlaanderenid) {
            rank = 1;
            label = "met plaats in de winst";
          } else if (finale == 1) {
            rank = 2;
            label = "met plaats in de finale";
          } else if (finale == 2) {
            rank = 3;
            label = "met plaats in de halve finale";
          } else {
            rank = 4;
            label = "met plaats in de kwartfinale";
          }

          if (rank < bestRank) {
            bestRank = rank;
            bestClub = clubnaam;
            bestLabel = label;
          }
        }

        if (bestClub != null && bestLabel != null) {
          return "Hoogst geplaatst in het tornooi van " + bestClub + " " + bestLabel;
        } else {
          return "Geen ranking gevonden voor speler met id: " + tennisvlaanderenid;
        }
      });
    } catch (Exception e) {
      throw new RuntimeException("Error retrieving ranking", e);
    }
    //throw new UnsupportedOperationException("Unimplemented method 'getHoogsteRankingVanSpeler'");
  }



  @Override
  public void addSpelerToTornooi(int tornooiId, int tennisvlaanderenId) {
    // TODO: verwijder de "throw new UnsupportedOperationException" en schrijf de code die de gewenste methode op de juiste manier implementeerd zodat de testen slagen.
    String sql = "INSERT INTO speler_speelt_tornooi (speler, tornooi) VALUES (:spelerId, :tornooiId)";
    try {
      jdbi.useHandle(handle -> {
        handle.createUpdate(sql)
                .bind("spelerId", tennisvlaanderenId)
                .bind("tornooiId", tornooiId)
                .execute();
      });
    } catch (Exception e) {
      throw new RuntimeException("Error adding speler to tornooi", e);
    }
   // throw new UnsupportedOperationException("Unimplemented method 'addSpelerToTornooi'");
  }

  @Override
  public void removeSpelerFromTornooi(int tornooiId, int tennisvlaanderenId) {
    // TODO: verwijder de "throw new UnsupportedOperationException" en schrijf de code die de gewenste methode op de juiste manier implementeerd zodat de testen slagen.
    String sql = "DELETE FROM speler_speelt_tornooi WHERE speler = :spelerId AND tornooi = :tornooiId";
    try {
      int rowsDeleted = jdbi.withHandle(handle ->
              handle.createUpdate(sql)
                      .bind("spelerId", tennisvlaanderenId)
                      .bind("tornooiId", tornooiId)
                      .execute()
      );

      if (rowsDeleted == 0) {
        throw new RuntimeException("No speler-tornooi link found to delete (speler=" +
                tennisvlaanderenId + ", tornooi=" + tornooiId + ")");
      }

    } catch (Exception e) {
      throw new RuntimeException("Error removing speler from tornooi", e);
    }
   // throw new UnsupportedOperationException("Unimplemented method 'removeSpelerFromTornooi'");
  }
}
