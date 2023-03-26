import com.sun.jdi.event.StepEvent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * This is just a demo for you, please run it on JDK17 (some statements may be not allowed in lower version).
 * This is just a demo, and you can extend and implement functions
 * based on this demo, or implement it in a different way.
 */
public class OnlineCoursesAnalyzer {

    List<Course> courses = new ArrayList<>();

    public OnlineCoursesAnalyzer(String datasetPath) {
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
                Course course = new Course(info[0], info[1], new Date(info[2]), info[3], info[4], info[5],
                        Integer.parseInt(info[6]), Integer.parseInt(info[7]), Integer.parseInt(info[8]),
                        Integer.parseInt(info[9]), Integer.parseInt(info[10]), Double.parseDouble(info[11]),
                        Double.parseDouble(info[12]), Double.parseDouble(info[13]), Double.parseDouble(info[14]),
                        Double.parseDouble(info[15]), Double.parseDouble(info[16]), Double.parseDouble(info[17]),
                        Double.parseDouble(info[18]), Double.parseDouble(info[19]), Double.parseDouble(info[20]),
                        Double.parseDouble(info[21]), Double.parseDouble(info[22]));
                courses.add(course);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //1
    public Map<String, Integer> getPtcpCountByInst() {
        Map<String, Integer> PtcpCountByInst = courses.stream().collect(Collectors.groupingBy(Course::getInstitution,Collectors.summingInt(Course::getParticipants)));
        return PtcpCountByInst;
    }

    //2
    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        Map<String, Integer> PtcpCountByInstAndSubject = new HashMap<>();
        for (Course course : courses) {
            String key = course.getInstitution() + "-" + course.getSubject();
            int count = PtcpCountByInstAndSubject.getOrDefault(key, 0);
            PtcpCountByInstAndSubject.put(key, count + course.getParticipants());
        }
        return PtcpCountByInstAndSubject.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    //3
    public Map<String, List<List<String>>> getCourseListOfInstructor() {
            Map<String, List<List<String>>> map = new HashMap<>();

            for (Course course : courses) {
                String[] instructors = course.getInstructors().split(",");
                for (String instructor : instructors) {
                    String key = instructor.trim();
                    List<List<String>> big = new ArrayList<>();

                    big.add(new ArrayList<>());
                    big.add(new ArrayList<>());

                        if(map.get(key)==null ){
                            map.put(key,big);
                        }

                            if(course.isIndependent()){
                                if(!map.get(key).get(0).contains(course.getTitle())){
                                    map.get(key).get(0).add(course.getTitle());
                                }

                            }else {
                                if(!map.get(key).get(1).contains(course.getTitle())){
                                    map.get(key).get(1).add(course.getTitle());
                                }
                            }

                }
            }
            for (List<List<String>> lists : map.values()) {
                for (List<String> list : lists) {
                    Collections.sort(list);
                }
            }

            for (String key : map.keySet()) {
                System.out.println(key+" == "+map.get(key).toString());
            }

            return map;
    }

    public static void main(String[] args) {

    }

    //4
    public List<String> getCourses(int topK, String by) {
        if (by.equals("hours")) {
            // 按照总课时（totalhours）属性对课程进行排序
            Collections.sort(courses, new Comparator<Course>() {
                @Override
                public int compare(Course o1, Course o2) {
                    if (o1.getTotalHours() > o2.getTotalHours()) {
                        return -1;
                    } else if (o1.getTotalHours() < o2.getTotalHours()) {
                        return 1;
                    } else {
                        return o1.getTitle().compareTo(o2.getTitle());
                    }
                }
            });
        } else if (by.equals("participants")) {
            // 按照参与者数量（Participants）属性对课程进行排序
            Collections.sort(courses, new Comparator<Course>() {
                @Override
                public int compare(Course o1, Course o2) {
                    if (o1.getParticipants() > o2.getParticipants()) {
                        return -1;
                    } else if (o1.getParticipants() < o2.getParticipants()) {
                        return 1;
                    } else {
                        return o1.getTitle().compareTo(o2.getTitle());
                    }
                }
            });
        }
        // 将排好序的课程标题存储在一个List<String>中并返回它
        List<String> result = new ArrayList<>();
        Set<String> titleSet = new HashSet<>();
        for (Course course : courses) {
            if (!titleSet.contains(course.getTitle())) {
                result.add(course.getTitle());
                titleSet.add(course.getTitle());
                if (result.size() == topK) {
                    break;
                }
            }
        }
        return result;
    }

    //5
    public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
        List<String> CL = new ArrayList<>();
        for (Course course : courses) {
            if (course.getSubject().toLowerCase().contains(courseSubject.toLowerCase())
                    && course.getPercentAudited() >= percentAudited
                    && course.getTotalHours() <= totalCourseHours) {
                if (!CL.contains(course.getTitle())) {
                CL.add(course.getTitle());
                }
            }
        }
        Collections.sort(CL);
        return CL;
    }

    //6
    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {

        Map<String , List<Double>> ncourses = new HashMap<>();
        //算出每个number对应的三个数据并存为map
        courses.stream().collect(Collectors.groupingBy(Course:: getNumber)).forEach((a,b) -> {
            double sumage = b.stream().mapToDouble(Course::getMedianAge).sum();
            double sumpercentMale = b.stream().mapToDouble(Course::getPercentMale).sum();
            double sumB = b.stream().mapToDouble(Course:: getPercentDegree).sum();
            double avsumage = sumage/b.size();
            double avsumpercentMale = sumpercentMale/b.size();
            double avsumB = sumB/b.size();
            List<Double> dl = new ArrayList<>();
            dl.add(avsumage);
            dl.add(avsumpercentMale);
            dl.add(avsumB);
            ncourses.put(a,dl);
        });
        System.out.println(ncourses);

        //算出每个number对应的相似度的值并存为一个map
        Map<String, Double> ncourses1 = new HashMap<>();

        ncourses.forEach((k,v) -> {
            double simivalue = Math.pow((age - v.get(0)),2)
                    + Math.pow((gender * 100 - v.get(1)),2)
                    + Math.pow((isBachelorOrHigher * 100 - v.get(2)),2);
            ncourses1.put(k, simivalue);
        });
        System.out.println(ncourses1);

        //按照每个number的相似度的值从小到大进行排序并存为一个map
        Map<String,Double> ncourse2 =  ncourses1.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new));
        System.out.println(ncourse2);

        //找出每个number对应的最新日期的课程
        Map<String, Course> ncourse3 = new HashMap<>();
        for(Course course : courses){
            if (ncourse3.get(course.getNumber()) == null){
                ncourse3.put(course.getNumber(),course);
            }else {
                if(course.getLaunchDate().after(ncourse3.get(course.getNumber()).getLaunchDate())){
                    ncourse3.put(course.getNumber(),course);
                }
            }
        }

        //找出每个number对应的最新日期的课程并按照相似度排序
        List<String> ncourse6 = new ArrayList<>();
        for (String s : ncourse2.keySet()){
            if(!ncourse6.contains(ncourse3.get(s).getTitle())){
                ncourse6.add(ncourse3.get(s).getTitle());
            }
            if (ncourse6.size() == 10){
                break;
            }
        }
        System.out.println(ncourse6);

        return ncourse6;
        //找出每个number对应的最新日期的课程的title
//        Map<String, String> ncourse4 = new HashMap<>();
//        ncourse6.forEach((k,v) -> {
//            ncourse4.put(k,v.getTitle());
//        });

        //找出相似度最小的十个number
//        Map<String, Double> courses10 = ncourse2.entrySet().stream()
//                .limit(10)
//                .collect(Collectors.toMap(
//                        Map.Entry::getKey,
//                        Map.Entry::getValue,
//                        (oldValue, newValue) -> oldValue,
//                        LinkedHashMap::new
//                ));
//        System.out.println(courses10);

        //把这十个number对应的title放进list并返回
//        List<String> recommend = new ArrayList<>();
//
//        for (String s : ncourse4.keySet()){
//            if(!recommend.contains(ncourse4.get(s))){
//                recommend.add(ncourse4.get(s));
//            }
//            if(recommend.size() == 10){
//                break;
//            }
//        }
//
//        return recommend;

    }



}

class Course {
    String institution;
    String number;
    Date launchDate;
    String title;
    String instructors;
    String subject;
    int year;
    int honorCode;
    int participants;
    int audited;
    int certified;
    double percentAudited;
    double percentCertified;
    double percentCertified50;
    double percentVideo;
    double percentForum;
    double gradeHigherZero;
    double totalHours;
    double medianHoursCertification;
    double medianAge;
    double percentMale;
    double percentFemale;
    double percentDegree;

    public boolean isIndependent() {
        return instructors.split(",").length == 1;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Date getLaunchDate() {
        return launchDate;
    }

    public void setLaunchDate(Date launchDate) {
        this.launchDate = launchDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInstructors() {
        return instructors;
    }

    public void setInstructors(String instructors) {
        this.instructors = instructors;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getHonorCode() {
        return honorCode;
    }

    public void setHonorCode(int honorCode) {
        this.honorCode = honorCode;
    }

    public int getParticipants() {
        return participants;
    }

    public void setParticipants(int participants) {
        this.participants = participants;
    }

    public int getAudited() {
        return audited;
    }

    public void setAudited(int audited) {
        this.audited = audited;
    }

    public int getCertified() {
        return certified;
    }

    public void setCertified(int certified) {
        this.certified = certified;
    }

    public double getPercentAudited() {
        return percentAudited;
    }

    public void setPercentAudited(double percentAudited) {
        this.percentAudited = percentAudited;
    }

    public double getPercentCertified() {
        return percentCertified;
    }

    public void setPercentCertified(double percentCertified) {
        this.percentCertified = percentCertified;
    }

    public double getPercentCertified50() {
        return percentCertified50;
    }

    public void setPercentCertified50(double percentCertified50) {
        this.percentCertified50 = percentCertified50;
    }

    public double getPercentVideo() {
        return percentVideo;
    }

    public void setPercentVideo(double percentVideo) {
        this.percentVideo = percentVideo;
    }

    public double getPercentForum() {
        return percentForum;
    }

    public void setPercentForum(double percentForum) {
        this.percentForum = percentForum;
    }

    public double getGradeHigherZero() {
        return gradeHigherZero;
    }

    public void setGradeHigherZero(double gradeHigherZero) {
        this.gradeHigherZero = gradeHigherZero;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }

    public double getMedianHoursCertification() {
        return medianHoursCertification;
    }

    public void setMedianHoursCertification(double medianHoursCertification) {
        this.medianHoursCertification = medianHoursCertification;
    }

    public double getMedianAge() {
        return medianAge;
    }

    public void setMedianAge(double medianAge) {
        this.medianAge = medianAge;
    }

    public double getPercentMale() {
        return percentMale;
    }

    public void setPercentMale(double percentMale) {
        this.percentMale = percentMale;
    }

    public double getPercentFemale() {
        return percentFemale;
    }

    public void setPercentFemale(double percentFemale) {
        this.percentFemale = percentFemale;
    }

    public double getPercentDegree() {
        return percentDegree;
    }

    public void setPercentDegree(double percentDegree) {
        this.percentDegree = percentDegree;
    }

    public Course(String institution, String number, Date launchDate,
                  String title, String instructors, String subject,
                  int year, int honorCode, int participants,
                  int audited, int certified, double percentAudited,
                  double percentCertified, double percentCertified50,
                  double percentVideo, double percentForum, double gradeHigherZero,
                  double totalHours, double medianHoursCertification,
                  double medianAge, double percentMale, double percentFemale,
                  double percentDegree) {
        this.institution = institution;
        this.number = number;
        this.launchDate = launchDate;
        if (title.startsWith("\"")) title = title.substring(1);
        if (title.endsWith("\"")) title = title.substring(0, title.length() - 1);
        this.title = title;
        if (instructors.startsWith("\"")) instructors = instructors.substring(1);
        if (instructors.endsWith("\"")) instructors = instructors.substring(0, instructors.length() - 1);
        this.instructors = instructors;
        if (subject.startsWith("\"")) subject = subject.substring(1);
        if (subject.endsWith("\"")) subject = subject.substring(0, subject.length() - 1);
        this.subject = subject;
        this.year = year;
        this.honorCode = honorCode;
        this.participants = participants;
        this.audited = audited;
        this.certified = certified;
        this.percentAudited = percentAudited;
        this.percentCertified = percentCertified;
        this.percentCertified50 = percentCertified50;
        this.percentVideo = percentVideo;
        this.percentForum = percentForum;
        this.gradeHigherZero = gradeHigherZero;
        this.totalHours = totalHours;
        this.medianHoursCertification = medianHoursCertification;
        this.medianAge = medianAge;
        this.percentMale = percentMale;
        this.percentFemale = percentFemale;
        this.percentDegree = percentDegree;
    }
}