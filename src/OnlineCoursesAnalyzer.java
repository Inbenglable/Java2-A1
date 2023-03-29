import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

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
            Double.parseDouble(info[12]), Double.parseDouble(info[13]),
            Double.parseDouble(info[14]),
            Double.parseDouble(info[15]), Double.parseDouble(info[16]),
            Double.parseDouble(info[17]),
            Double.parseDouble(info[18]), Double.parseDouble(info[19]),
            Double.parseDouble(info[20]),
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
    return courses.stream()
        .collect(Collectors.groupingBy(
            a -> a.institution, Collectors.summingInt(a -> a.participants)));
  }

  class Two {

    String name;
    Integer value;

    public Two(String institution, String subject, Integer count) {
      name = String.format("%s-%s", institution, subject);
      value = count;
    }
  }

  //2
  public Map<String, Integer> getPtcpCountByInstAndSubject() {
    Map<String, Integer> temp = courses.stream()
        .map(a -> new Two(a.institution, a.subject, a.participants))
        .collect(Collectors.groupingBy(a -> a.name, Collectors.summingInt(b -> b.value)));
    List<Map.Entry<String, Integer>> temp2 = new ArrayList<>(temp.entrySet());
    temp2.sort((b, a) -> {
      if (!Objects.equals(a.getValue(), b.getValue())) {
        return a.getValue() - b.getValue();
      } else {
        return b.getKey().compareTo(a.getKey());
      }
    });
    LinkedHashMap<String, Integer> temp3 = new LinkedHashMap<>();
    temp2.forEach(a -> temp3.put(a.getKey(), a.getValue()));
    return temp3;
  }

  class Three {

    String[] instructors;
    String title;

    public Three(String[] instr, String title) {
      this.title = title;
      instructors = instr;
    }
  }

  //3
  public Map<String, List<List<String>>> getCourseListOfInstructor() {
    List<Three> temp = courses.stream().map(a -> new Three(a.instructors.split(", "), a.title))
        .collect(Collectors.toList());
    Map<String, List<HashSet<String>>> temp1 = new HashMap<>();
    for (Three x : temp) {
      if (x.instructors.length > 1) {
        for (String m : x.instructors) {
          if (!temp1.containsKey(m)) {
            List<HashSet<String>> a = new ArrayList<>();
            a.add(new HashSet<>());
            a.add(new HashSet<>());
            a.get(1).add(x.title);
            temp1.put(m, a);
          } else {
            temp1.get(m).get(1).add(x.title);
          }
        }
      } else if (x.instructors.length == 1) {
        if (!temp1.containsKey(x.instructors[0])) {
          List<HashSet<String>> a = new ArrayList<>();
          a.add(new HashSet<>());
          a.add(new HashSet<>());
          a.get(0).add(x.title);
          temp1.put(x.instructors[0], a);
        } else {
          temp1.get(x.instructors[0]).get(0).add(x.title);
        }
      }
    }
    Map<String, List<List<String>>> temp2 = new HashMap<>();
    for (String key : temp1.keySet()) {
      List<List<String>> a = new ArrayList<>();
      a.add(new ArrayList<>(temp1.get(key).get(0)));
      a.add(new ArrayList<>(temp1.get(key).get(1)));
      temp2.put(key, a);
    }
    for (String x : temp2.keySet()) {
      temp2.get(x).get(0).sort(String::compareTo);
      temp2.get(x).get(1).sort(String::compareTo);
    }
    return temp2;
  }

  //4
  public List<String> getCourses(int topK, String by) {
    List<String> ans;
    if (by.equals("hours")) {
      ans = courses.stream().sorted(
          (a, b) -> {
            if (b.totalHours != a.totalHours) {
              return (int) (b.totalHours - a.totalHours);
            } else {
              return a.title.compareTo(b.title);
            }
          }).map(a -> a.title).distinct().collect(Collectors.toList());
    } else {
      ans = courses.stream().sorted(
          (a, b) -> {
            if (b.participants != a.participants) {
              return b.participants - a.participants;
            } else {
              return a.title.compareTo(b.title);
            }
          }).map(a -> a.title).distinct().collect(Collectors.toList());
    }
    return ans.subList(0, topK);

  }

  //5
  public List<String> searchCourses(String courseSubject, double percentAudited,
      double totalCourseHours) {
    return courses.stream()
        .filter(a -> a.percentAudited >= percentAudited && a.totalHours <= totalCourseHours
            && a.subject.toLowerCase().contains(courseSubject.toLowerCase())).map(a -> a.title)
        .distinct().sorted(String::compareTo).toList();
  }

  class Six {

    Date date;
    String name;

    public Six(Date date, String name) {
      this.date = date;
      this.name = name;
    }
  }

  //6
  public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
    HashMap<String, Six> latestNum = new HashMap<>();
    courses.forEach(a -> latestNum.merge(a.number, new Six(a.launchDate, a.title), (b, c) -> {
      if (b.date.compareTo(c.date) < 0) {
        return c;
      } else {
        return b;
      }
    }));
    Map<String, Double> avgAge = courses.stream()
        .collect(
            Collectors.groupingBy(a -> a.number, Collectors.averagingDouble(a -> a.medianAge)));
    Map<String, Double> avgPerMale = courses.stream()
        .collect(
            Collectors.groupingBy(a -> a.number, Collectors.averagingDouble(a -> a.percentMale)));
    Map<String, Double> avgPerDegree = courses.stream()
        .collect(
            Collectors.groupingBy(a -> a.number, Collectors.averagingDouble(a -> a.percentDegree)));

    Iterator<String> it = avgAge.keySet().iterator();
    Map<String, Double> temp = new HashMap<>();
    while (it.hasNext()) {
      String key = it.next();
      Double value =
          Math.pow((age - avgAge.get(key)), 2) + Math.pow((gender * 100 - avgPerMale.get(key)), 2)
              + Math.pow((isBachelorOrHigher * 100 - avgPerDegree.get(key)), 2);
      temp.merge(latestNum.get(key).name, value, (a, b) -> a < b ? a : b);
    }
    return temp.entrySet().stream().sorted((a, b) -> {
      if (!Objects.equals(a.getValue(), b.getValue())) {
        return (int) (a.getValue() - b.getValue());
      } else {
        return a.getKey().compareTo(b.getKey());
      }
    }).map(Entry::getKey).distinct().toList().subList(0, 10);
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
    if (title.startsWith("\"")) {
      title = title.substring(1);
    }
    if (title.endsWith("\"")) {
      title = title.substring(0, title.length() - 1);
    }
    this.title = title;
    if (instructors.startsWith("\"")) {
      instructors = instructors.substring(1);
    }
    if (instructors.endsWith("\"")) {
      instructors = instructors.substring(0, instructors.length() - 1);
    }
    this.instructors = instructors;
    if (subject.startsWith("\"")) {
      subject = subject.substring(1);
    }
    if (subject.endsWith("\"")) {
      subject = subject.substring(0, subject.length() - 1);
    }
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