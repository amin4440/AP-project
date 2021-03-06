package Model.Factories;

import Model.Farm;
import Model.GameMenu.Game;
import Model.Item;
import Model.Positions.MapPosition;
import Model.Upgradable;
import Model.Warehouse;
import View.Factories.FactoryView;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import controller.ImageViewSprite;
import controller.InputProcessor;
import controller.Main;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;

public class Factory implements Upgradable {
    public static final String FactoriesConfigFilePath = "FactoriesConfigFile.json";
    public static ArrayList<FactoryType> factoryTypeArrayList = new ArrayList<>(0);

    static {
        File file = new File(FactoriesConfigFilePath);
        try {
            FileReader fileReader = new FileReader(FactoriesConfigFilePath);
            StringBuilder stringBuilder = new StringBuilder();
            Scanner scanner = new Scanner(fileReader);

            while (scanner.hasNext()) {
                stringBuilder.append(scanner.nextLine());
            }

            Gson gson = InputProcessor.gson;

            factoryTypeArrayList.addAll(gson.fromJson(stringBuilder.toString(), new TypeToken<ArrayList<FactoryType>>() {
            }.getType()));
            //factoryTypeArrayList = (ArrayList<FactoryType>) types;
            //System.out.println("DSasd");


        } catch (FileNotFoundException e) {
            FactoryView.permissionDeniedToReadFactoriesConfigFile();
            //todo probably better to move it to view
        } /*catch (IOException e) {
            FactoryView.unableToMakeFactoriesConfigFile();
        }*/


    }

    String path = "/home/a/Projects/AP_Project/AP_15/static/Workshops";
    FactoryType factoryType;
    MapPosition outputPosition;
    Process process;
    int Level;
    ImageView imageView;
    ImageViewSprite sprite;

    public Factory(FactoryType factoryType, MapPosition outputPosition, Process process, int level) {
        this.factoryType = factoryType;
        this.outputPosition = outputPosition;
        this.process = process;
        Level = level;
        File file = new File("./static/Workshops");
        for (File file1 : Objects.requireNonNull(file.listFiles())) {
            if (file1.getName().contains(this.factoryType.name)) {
                try {
                    factoryType.image = new Image(new FileInputStream(file1.getAbsolutePath() + "/0" + String.valueOf(level + 1) + ".png"));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        //gridPane = Main.gridPane;
    }

    /*public static void controller.Main(String[] args) {

        try {
            FileReader fileReader = new FileReader(FactoriesConfigFilePath);
            Scanner scanner = new Scanner(fileReader);
            String Json = scanner.nextLine();
            Gson gson = new Gson();
            Type collectionType = new TypeToken<HashSet<FactoryType>>() {
            }.getType();
            HashSet<FactoryType> II = (gson.fromJson(Json, collectionType));
            System.out.println(II);
            //fail();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //assertTrue(true);
        }


    }*/

    public static FactoryType findFactoryType(String name) {
        for (FactoryType factoryType : factoryTypeArrayList) {
            if (factoryType.getName().equalsIgnoreCase(name)) {
                return factoryType;
            }
        }
        return null;
    }

    public FactoryType getFactoryType() {
        return factoryType;
    }

    public void setFactoryType(FactoryType factoryType) {
        this.factoryType = factoryType;
    }

    public boolean turn() {
        boolean flag=false;
        synchronized (Game.obj) {
            if (process != null) {
                if (process.getRemainedTurns() > 1) {
                    process.reduceRemainedTurnsByOne();
                } else if (process.getRemainedTurns() == 1) {
                    process.reduceRemainedTurnsByOne();
                    finishProcess();

                    flag= true;
                } else {
                    // doing nothing now but nothing else
                }
            }
        }
        System.out.println("Weefsjdfhk");
        Object obj = new Object();
        synchronized (obj) {
            try {
                obj.wait(InputProcessor.getSpeed()*10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        InputProcessor.game.getFarm().getMap().threads.add(new Thread(() -> turn()));

        return flag;
    }

    private void finishProcess() {
        sprite.stop();
        sprite = new ImageViewSprite(imageView, factoryType.image, 4, 4, 16, (int) (factoryType.image.getWidth() / 4.0), (int) (factoryType.image.getHeight() / 4), 30, 0, 0, false);
        sprite.start();
        sprite.stop();


        Item outputItem = Item.getInstance(factoryType.OutputItem.getItemName());
        outputItem.setPosition(outputPosition);


    }

    public boolean startProcess(Warehouse warehouse) {
        List<Item> items = warehouse.getItems();
        int min = 1000;
        if (process != null) {
            System.out.println("Factory Already doing some process");
            return false;
        }

        for (FactoryType.Isp isp : this.getFactoryType().InputItems) {

            int num = 0;
            for (Item item : items) {
                if (item.getItemInfo().equals(isp.itemInfo)) {
                    num += 1;
                }

            }

            min = Math.min(min, num / isp.weight);
        }
        if (min > this.factoryType.Ts.get(Level).ProductionNum) {
            min = this.factoryType.Ts.get(Level).ProductionNum;
        }
        if (min > 0) {


            Outer:
            for (FactoryType.Isp isp : this.getFactoryType().InputItems) {
                Iterator<Item> itemIterator = items.iterator();
                Item item;
                while (itemIterator.hasNext()) {
                    item = itemIterator.next();
                    int temp = 0;
                    if (item.getItemInfo().equals(isp.itemInfo)) {
                        itemIterator.remove();
                        temp += 1;
                        if (temp == min) {
                            continue Outer;
                        }
                    }
                }


            }


            process = new Process(getNeededTurns(), min);
            sprite = new ImageViewSprite(imageView, factoryType.image, 4, 4, 16, (int) (factoryType.image.getWidth() / 4.0), (int) (factoryType.image.getHeight() / 4), 30, 0, 0, false);
            sprite.start();
            return true;
        } else {
            System.out.println("Not Any Ingredients");
            return false;
        }


    }

    private double getNeededTurns() {
        return this.factoryType.Ts.get(Level).MaxProductionTime;
    }

    private boolean isFinished() {
        return false;
    }

    @Override
    public boolean upgrade(Farm farm) {
        if (Level == this.factoryType.Ts.size() - 1) {
            System.out.println("Fully Upgraded");
            return false;
        } else {
            if (farm.getCurrentMoney() < getUpgradeCost()) {
                System.out.println("You don't have enough money");
                return false;
            } else {
                farm.setCurrentMoney(farm.getCurrentMoney() - getUpgradeCost());
                Level += 1;
                File file = new File("./static/Workshops");
                for (File file1 : Objects.requireNonNull(file.listFiles())) {
                    if (file1.getName().contains(this.factoryType.name)) {
                        try {
                            factoryType.image = new Image(new FileInputStream(file1.getAbsolutePath() + "/0" + String.valueOf(Level + 1) + ".png"));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
                sprite.imageView.setImage(factoryType.image);
                return true;
            }
        }

    }

    @Override
    public int getUpgradeCost() {
        return this.factoryType.Ts.get(Level).InGameCost;
    }

    public void print() {
        System.out.println(factoryType.name);
        System.out.println("Level = " + Level);
        //System.out.println(factoryType);
    }

    public void show() {
        if (imageView == null || !(Main.pane.getChildren().contains(imageView))) {
            imageView = new ImageView(factoryType.image);
            Main.pane.getChildren().add(imageView);
            //AnchorPane.setTopAnchor(imageView, Arrays.asList(InputProcessor.game.getFarm().factories).indexOf(this) * 50.0 + 100.0);
            //AnchorPane.setLeftAnchor(imageView, 70.0);
            imageView.setX(70.0);
            imageView.setY(Arrays.asList(InputProcessor.game.getFarm().factories).indexOf(this) * 50.0 + 100.0);
            imageView.setOnMouseClicked(keyEvent -> this.startProcess(InputProcessor.game.getFarm().getWarehouse()));

        }


        //ImageView imageView = new ImageView();
        //Image image1 = new Image((new FileInputStream("/home/a/Projects/AP_Project/AP_15/static/Workshops/Cake (Cookie Bakery)/01.png")));
        sprite = new ImageViewSprite(imageView, factoryType.image, 4, 4, 16, (int) (factoryType.image.getWidth() / 4.0), (int) (factoryType.image.getHeight() / 4), 30, 0, 0, false);
        //Main.pane.getChildren().add(imageView);
        sprite.start();
        if (process == null) {
            sprite.stop();
        }


    }

    public static class Process {
        double remainedTurns;
        int numberOfInputs;
        int numberOfOutputs;


        public Process(double remainedTurns, int numberOfOutputs) {
            this.remainedTurns = remainedTurns;
            //if (numberOfOutputs<)
            this.numberOfOutputs = numberOfOutputs;
        }

        public void reduceRemainedTurnsByOne() {
            setRemainedTurns(getRemainedTurns() - 1);
        }

        public double getRemainedTurns() {
            return remainedTurns;
        }

        public void setRemainedTurns(double remainedTurns) {
            this.remainedTurns = remainedTurns;
        }

        public int getNumberOfInputs() {
            return numberOfInputs;
        }

        public void setNumberOfInputs(int numberOfInputs) {
            this.numberOfInputs = numberOfInputs;
        }

        public int getNumberOfOutputs() {
            return numberOfOutputs;
        }

        public void setNumberOfOutputs(int numberOfOutputs) {
            this.numberOfOutputs = numberOfOutputs;
        }
    }

    public static class FactoryTypeDeserializer implements JsonDeserializer<FactoryType> {


        @Override
        public FactoryType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String name = jsonDeserializationContext.deserialize(jsonObject.get("name"), String.class);
            int numberOfInputItems = jsonDeserializationContext.deserialize(jsonObject.get("numberOfInputItems"), int.class);
            int numberOfOutputItems = jsonDeserializationContext.deserialize(jsonObject.get("numberOfOutputItems"), int.class);
            int ProcessTurns = jsonDeserializationContext.deserialize(jsonObject.get("ProcessTurns"), int.class);
            ArrayList<FactoryType.t> InputItems = jsonDeserializationContext.deserialize(jsonObject.get("InputItems"), new TypeToken<ArrayList<FactoryType.t>>() {
            }.getType());
            return new FactoryType(name, InputItems);
        }
    }

    public static class FactoryType {
        public Image image;
        String name;
        Item.ItemInfo OutputItem;
        ArrayList<Isp> InputItems;
        int numberOfInputItems;
        int numberOfOutputItems;
        int ProcessTurns;
        ArrayList<FactoryType.t> Ts = new ArrayList<>(0);

        public FactoryType(String name, ArrayList<t> ts) {
            this.name = name;
            Ts = ts;
            switch (name) {

            }

        }

        public ArrayList<Isp> getInputItems() {
            return InputItems;
        }

        public void setInputItems(ArrayList<Isp> inputItems) {
            InputItems = inputItems;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Item.ItemInfo getOutputItem() {
            return OutputItem;
        }

        public void setOutputItem(Item.ItemInfo outputItem) {
            OutputItem = outputItem;
        }

        public int getNumberOfInputItems() {
            return numberOfInputItems;
        }

        public void setNumberOfInputItems(int numberOfInputItems) {
            this.numberOfInputItems = numberOfInputItems;
        }

        public int getNumberOfOutputItems() {
            return numberOfOutputItems;
        }

        public void setNumberOfOutputItems(int numberOfOutputItems) {
            this.numberOfOutputItems = numberOfOutputItems;
        }

        public int getProcessTurns() {
            return ProcessTurns;
        }

        public void setProcessTurns(int processTurns) {
            ProcessTurns = processTurns;
        }

        public static class t {
            int Level;
            int ProductionNum;
            double MaxProductionTime;
            int MaxLevelCost;
            int InGameCost;

            public t(int level, int productionNum, double maxProductionTime, int maxLevelCost, int inGameCost) {
                Level = level;
                ProductionNum = productionNum;
                MaxProductionTime = maxProductionTime;
                MaxLevelCost = maxLevelCost;
                InGameCost = inGameCost;
            }
        }

        public static class Isp {
            Integer weight;
            Item.ItemInfo itemInfo;

            public Isp(Integer weight, Item.ItemInfo itemInfo) {
                this.weight = weight;
                this.itemInfo = itemInfo;
            }
        }


        //todo Item input
    }
}

