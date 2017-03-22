package com.dev.raja.http;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;


public class IOUtil {

    public static String readData(InputStream inputstream, boolean closeStream) throws IOException {

        BufferedReader bufferedreader = null;
        InputStreamReader inputstreamreader = null;

        try {
            inputstreamreader = new InputStreamReader(inputstream, "UTF-8");
            bufferedreader = new BufferedReader(inputstreamreader);
            StringBuilder strBuff = new StringBuilder();
            String responseStr = null;
            while ((responseStr = bufferedreader.readLine()) != null) {
                strBuff.append(responseStr);
            }

            return strBuff.toString();
        } finally {

            if (closeStream) {
                inputstream.close();
            }

            if (inputstreamreader != null) {
                inputstreamreader.close();
            }

            if (bufferedreader != null) {
                try {
                    bufferedreader.close();
                } catch (IOException e) {
                    System.out.println("IOException while closing buffered reader [{}]" + e.toString());
//                    logger.warn("IOException while closing buffered reader [{}]", e.toString());
                }
            }
        }
    }

    public static List<String> readFile(InputStream inputstream, boolean closeStream, boolean skipHeader) throws IOException {

        BufferedReader bufferedreader = null;
        InputStreamReader inputstreamreader = null;

        try {

            inputstreamreader = new InputStreamReader(inputstream);
            bufferedreader = new BufferedReader(inputstreamreader);
            String responseStr = null;
            List<String> dataList = new ArrayList<>();
            if (skipHeader)
                bufferedreader.readLine();
            while ((responseStr = bufferedreader.readLine()) != null) {
                dataList.add(responseStr);

            }

            return dataList;
        } finally {
            if (closeStream) {
                inputstream.close();
            }

            if (inputstreamreader != null) {
                inputstreamreader.close();
            }

            if (bufferedreader != null) {
                try {
                    bufferedreader.close();
                } catch (IOException e) {
                    System.out.println("IOException while closing buffered reader [{}]" + e.toString());
//                    logger.warn("IOException while closing buffered reader [{}]", e.toString());
                }
            }
        }
    }

    public static boolean isValidImageExtn(String extn) {
        return (extn.endsWith("jpg") || extn.endsWith("jpeg") || extn.endsWith("gif") || extn.endsWith("png"));
    }

    public static void writeToFile(String fileName, Map<String, ? extends Object> map) {
        writeToFile(fileName, map, true);
    }

    public static void writeToFile(String fileName, Map<String, ? extends Object> map, boolean gzip) {
        BufferedWriter bw = null;
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            GZIPOutputStream gos = null;
            if (gzip)
                gos = new GZIPOutputStream(fos);

            bw = new BufferedWriter(new OutputStreamWriter(gzip ? gos : fos));
            for (Entry<String, ? extends Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                String line = value.toString();
                bw.write(line);
                bw.write('\n');
                // gos.write(line.getBytes("UTF-8"));
                // gos.write('\n');
            }
            if (gzip) {
                gos.finish();
                gos.close();
            }
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    System.out.println("IOException while closing buffered reader [{}]" + e.toString());
                }
            }
        }
    }


    public static void writeToFile(String fileName, Collection<? extends Object> objList, boolean gzip) {
        BufferedWriter bw = null;
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            GZIPOutputStream gos = null;
            if (gzip)
                gos = new GZIPOutputStream(fos);

            bw = new BufferedWriter(new OutputStreamWriter(gzip ? gos : fos));

            for (Iterator iterator = objList.iterator(); iterator.hasNext(); ) {
                Object next = iterator.next();
                String line = next.toString();
                bw.write(line);
                bw.write('\n');
            }

            if (gzip) {
                gos.finish();
                gos.close();
            }
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    System.out.println("IOException while closing buffered reader [{}]" + e.toString());
                }
            }
        }
    }

    public static List<String> readLines(String data, boolean lowerCase) throws IOException {
        BufferedReader bufferedreader = null;
        try {
            bufferedreader = new BufferedReader(new StringReader(data));
            String line = null;

            List<String> dataList = new ArrayList<>();
            while ((line = bufferedreader.readLine()) != null) {
                if (lowerCase)
                    dataList.add(line.trim().toLowerCase());
                else
                    dataList.add(line);
            }
            return dataList;
        } finally {
            if (bufferedreader != null) {
                bufferedreader.close();
            }
        }
    }

    public static void writeToFile(String fileName, String fileContent) {
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            GZIPOutputStream gos = new GZIPOutputStream(fos);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(gos));
            bw.write(fileContent);
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeToFile(String path, String fileName, String fileContent) {

        Path sourcePath = Paths.get(path);
        try {
            Files.createDirectories(sourcePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sourcePath = Paths.get(path + "/" + fileName);
        try {
            Files.write(sourcePath, fileContent.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}