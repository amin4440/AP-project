package Model.Animals;

import Model.Farm;
import Model.Item;
import Model.Positions.MapPosition;
import Model.Upgradable;
import controller.InputProcessor;

import java.util.ListIterator;

public class Cage implements Upgradable {
    public WildAnimal getWildAnimal() {
        return wildAnimal;
    }

    public void setWildAnimal(WildAnimal wildAnimal) {
        this.wildAnimal = wildAnimal;
    }

    transient WildAnimal wildAnimal;
    int CompletnessPercetage;
    int remainingTimeTo;
    int turnsCompleted;

    public Cage(int remainingTimeTo, int numOfWildAnimalsInTheCell) {
        CompletnessPercetage = 0;

    }

    public Cage(WildAnimal wildAnimal) {
        this.wildAnimal =wildAnimal;
    }

    public int getCompletnessPercetage() {
        return CompletnessPercetage;
    }

    public void setCompletnessPercetage(int completnessPercetage) {
        CompletnessPercetage = completnessPercetage;
    }

    public int getCagingPrice(int numberOfWildAnimalsInTheCell) {
        //todo
        return 0;
    }

    public void addCompletenesPercentage() {
        CompletnessPercetage += 20;
        if (CompletnessPercetage >= getProgressMaxValue()) {
            if (CompletnessPercetage ==getProgressMaxValue()+20){
                System.out.println("It's already completed:the cage");
            }else {
                turnsCompleted =0;
            }
            CompletnessPercetage = getProgressMaxValue();




        }

    }

    public void turn() {
        if (wildAnimal.isCaged()){
            turnsCompleted++;
        }
        if (turnsCompleted == getEscapeTurn() && wildAnimal.getPosition() instanceof MapPosition) {
            wildAnimal.escape();
        }
        if (getCompletnessPercetage() != getProgressMaxValue()) {

            setCompletnessPercetage(getCompletnessPercetage() - 5);
        }
        if (getCompletnessPercetage() <= 0) {
            wildAnimal.setCage(null);
            setCompletnessPercetage(0);
        }

    }

    private int getEscapeTurn() {
        return 6 + 2 * InputProcessor.game.getFarm().getCagesLevel();
    }

    @Override
    public boolean upgrade(Farm farm) {

        Integer integer = InputProcessor.game.getFarm().getCagesLevel();
        if (integer == 3) {
            System.out.println("More upgrade not possible");
            return false;
        } else if (farm.getCurrentMoney() < getUpgradeCost()) {
            System.out.println("Not Enough money the specified upgrade");
            return false;
        } else {
            farm.setCurrentMoney(farm.getCurrentMoney() - getUpgradeCost());
            integer = integer + 1;

            return true;


        }
    }

    @Override
    public int getUpgradeCost() {
        switch (InputProcessor.game.getFarm().getCagesLevel()) {
            case 0:
                return 0;
            case 1:
                return 100;
            case 2:
                return 500;
            case 3:
                return 5000;
            default:
                return 0;
        }
    }


    public int getProgressMaxValue() {
        switch (InputProcessor.game.getFarm().getCagesLevel()) {
            case 0:
                return 100;
            case 1:
                return 60;
            case 2:
                return 40;
            case 3:
                return 20;
            default:
                return 0;
        }
    }


}
