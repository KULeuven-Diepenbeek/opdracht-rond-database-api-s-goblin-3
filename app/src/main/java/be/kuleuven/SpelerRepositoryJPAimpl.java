package be.kuleuven;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class SpelerRepositoryJPAimpl implements SpelerRepository {
    private final EntityManager em;
    public static final String PERSISTANCE_UNIT_NAME = "be.kuleuven.spelerhibernateTest";

    // Constructor
    SpelerRepositoryJPAimpl(EntityManager entityManager) {
        // TODO: verwijder de "throw new UnsupportedOperationException" en schrijf de code die de gewenste constructor op de juiste manier implementeerd zodat de testen slagen.
        this.em=entityManager;
       // throw new UnsupportedOperationException("Unimplemented constructor");
    }// De jpa code heeft een probleem waarbij java lijkt de inhoud van de test database te vergeten.
    // Ik ben niet geheel zeker waar dit probleem vandaan komt. Aangezien het bij test 1 wel functioneert, neem ik aan dat er iets misloopt met de connection naar de DB.


    @Override
    public void addSpelerToDb(Speler speler) {
        // TODO: verwijder de "throw new UnsupportedOperationException" en schrijf de code die de gewenste methode op de juiste manier implementeerd zodat de testen slagen.
        try {
            em.getTransaction().begin();
            em.persist(speler);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (e.getMessage() != null && e.getMessage().contains("PRIMARY KEY")) {
                throw new RuntimeException("A PRIMARY KEY constraint failed", e);
            }
            throw new RuntimeException("Error inserting speler into DB", e);
        }
        //throw new UnsupportedOperationException("Unimplemented method 'getAllSpelers'");
    }

    @Override
    public Speler getSpelerByTennisvlaanderenId(int tennisvlaanderenId) {
        // TODO: verwijder de "throw new UnsupportedOperationException" en schrijf de code die de gewenste methode op de juiste manier implementeerd zodat de testen slagen.
        try {
            Speler speler = em.find(Speler.class, tennisvlaanderenId);
            if (speler == null) {
                throw new RuntimeException("Invalid Speler met identification: " + tennisvlaanderenId);
            }
            return speler;
        } catch (Exception e) {
            throw new RuntimeException("Invalid Speler met identification: " + tennisvlaanderenId);
        }
       // throw new UnsupportedOperationException("Unimplemented method 'getAllSpelers'");
    }

    @Override
    public List<Speler> getAllSpelers() {
        // TODO: verwijder de "throw new UnsupportedOperationException" en schrijf de code die de gewenste methode op de juiste manier implementeerd zodat de testen slagen.
        try {
            return em.createQuery("SELECT s FROM Speler s", Speler.class)
                    .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving all spelers", e);
        }
        //throw new UnsupportedOperationException("Unimplemented method 'getAllSpelers'");
    }

    @Override
    public void updateSpelerInDb(Speler speler) {
        // TODO: verwijder de "throw new UnsupportedOperationException" en schrijf de code die de gewenste methode op de juiste manier implementeerd zodat de testen slagen.
        try {
            em.getTransaction().begin();
            Speler speler1 = em.find(Speler.class,speler.getTennisvlaanderenid());
            if (speler1 == null){
                em.getTransaction().rollback();
                throw new RuntimeException("Invalid Speler met identification: " + speler.getTennisvlaanderenid());
            }
            speler1.setNaam(speler.getNaam());
            speler1.setPunten(speler.getPunten());
            em.getTransaction().commit();
        } catch (Exception e){
            if(em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }throw new RuntimeException("Error updating speler in DB", e);
        }
        //throw new UnsupportedOperationException("Unimplemented method 'getAllSpelers'");
    }

    @Override
    public void deleteSpelerInDb(int tennisvlaanderenId) {
        // TODO: verwijder de "throw new UnsupportedOperationException" en schrijf de code die de gewenste methode op de juiste manier implementeerd zodat de testen slagen.
        try {


            em.getTransaction().begin();
            Speler speler = em.find(Speler.class, tennisvlaanderenId);
            if (speler == null) {
                em.getTransaction().rollback();
                throw new RuntimeException("Invalid Speler met identification: " + tennisvlaanderenId);
            }
            em.remove(speler);
            em.getTransaction().commit();


        } catch (Exception e){
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error deleting speler from DB", e);
        }
        //throw new UnsupportedOperationException("Unimplemented method 'getAllSpelers'");
    }

    @Override
    public String getHoogsteRankingVanSpeler(int tennisvlaanderenId) {
        // TODO: verwijder de "throw new UnsupportedOperationException" en schrijf de code die de gewenste methode op de juiste manier implementeerd zodat de testen slagen.    String jpql = "SELECT w FROM Wedstrijd w WHERE w.speler1Id = :id OR w.speler2Id = :id";

        String jpql = "SELECT w FROM Wedstrijd w WHERE w.speler1Id = :id OR w.speler2Id = :id";

        try {
            List<Wedstrijd> wedstrijden = em.createQuery(jpql, Wedstrijd.class)
                    .setParameter("id", tennisvlaanderenId)
                    .getResultList();

            int bestRank = Integer.MAX_VALUE;
            String bestClub = null;
            String bestLabel = null;

            for (Wedstrijd w : wedstrijden) {
                int rank;
                String label;

                if (w.getFinale() == 1 && w.getWinnaarId() == tennisvlaanderenId) {
                    rank = 1;
                    label = "met plaats in de winst";
                } else if (w.getFinale() == 1) {
                    rank = 2;
                    label = "met plaats in de finale";
                } else if (w.getFinale() == 2) {
                    rank = 3;
                    label = "met plaats in de halve finale";
                } else {
                    rank = 4;
                    label = "met plaats in de kwartfinale";
                }

                if (rank < bestRank) {
                    bestRank = rank;
                    bestLabel = label;

                    Tornooi t = em.find(Tornooi.class, w.getTornooiId());
                    bestClub = (t != null) ? t.getClubnaam() : "Onbekend Tornooi";
                }
            }

            if (bestClub != null && bestLabel != null) {
                return "Hoogst geplaatst in het tornooi van " + bestClub + " " + bestLabel;
            } else {
                return "Geen ranking gevonden voor speler met id: " + tennisvlaanderenId;
            }

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving ranking", e);
        }




        //throw new UnsupportedOperationException("Unimplemented method 'getAllSpelers'");
    }

    @Override
    public void addSpelerToTornooi(int tornooiId, int tennisvlaanderenId) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Speler speler = em.find(Speler.class, tennisvlaanderenId);
            Tornooi tornooi = em.find(Tornooi.class, tornooiId);

            speler.getTornooien().add(tornooi);
            em.merge(speler);

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void removeSpelerFromTornooi(int tornooiId, int tennisvlaanderenId) {
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Speler speler = em.find(Speler.class, tennisvlaanderenId);
            Tornooi tornooi = em.find(Tornooi.class, tornooiId);

            if (speler == null || tornooi == null) {
                throw new IllegalArgumentException("Speler or Tornooi not found");
            }

            speler.getTornooien().remove(tornooi);
            em.merge(speler);

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
