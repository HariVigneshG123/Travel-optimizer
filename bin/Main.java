import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        String ip = "New York,Los Angeles,Chicago,Houston,Phoenix,Philadelphia,San Antonio,San Diego,Dallas,San Jose,Austin,Jacksonville,Fort Worth,Columbus,Indianapolis,Charlotte,San Francisco,Seattle,Denver,Washington,Nashville-Davidson,Oklahoma City,El Paso,Boston,Portland,Las Vegas,Detroit,Memphis,Louisville-Jefferson County,Baltimore,Milwaukee,Albuquerque,Tucson,Fresno,Sacramento,Kansas City,Mesa,Atlanta,Omaha,Colorado Springs,Raleigh,Long Beach,Virginia Beach,Miami,Oakland,Minneapolis,Tulsa,Bakersfield,Wichita,Arlington";
        String[] sourceArr = ip.split(",");
        int maxSize = sourceArr.length;
        int numberOfDests = 20;
        ArrayList<String> destinations = new ArrayList<>();

//        ArrayList<String> srcs = new ArrayList<>();
        ArrayList<String> srcsDuplicate = new ArrayList<>();
        for(String s: sourceArr){
            for(int i=0; i<numberOfDests; i++){
                srcsDuplicate.add(s);
            }
        }

        for(int i = 0; i<maxSize; i++){
            ArrayList<Integer> destIndex = pickFiveRandomIndex(maxSize, i, numberOfDests);
            for(Integer dest: destIndex){
                destinations.add(sourceArr[dest]);
            }
        }
        System.out.println(srcsDuplicate);
        System.out.println(destinations);
    }

    private static ArrayList<Integer> pickFiveRandomIndex(int maxSize, int i, int numberOfDests) {
        ArrayList<Integer> rands = new ArrayList<>();
        while (rands.size()<numberOfDests){
            int random_int = (int)Math.floor(Math.random()*(maxSize));
            if(random_int != i && !rands.contains(random_int)){
                rands.add(random_int);
            }
        }
        return rands;
    }
}