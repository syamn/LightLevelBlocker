package syam.LightLevelBlocker;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigurationManager {
	public final static Logger log = LightLevelBlocker.log;
	public final static String logPrefix = LightLevelBlocker.logPrefix;
	public final static String msgPrefix = LightLevelBlocker.msgPrefix;

	private JavaPlugin plugin;
	private FileConfiguration conf;

	private static File pluginDir = new File("plugins", "LightLevelBlocker");

	// 設定項目
	private final static String defaultMsg = "&cToo dark to destroy this block!";

	public byte blockLevel = 0;
	public String msg = defaultMsg;
	public boolean onlyStones = true;

	public List<String> bypassWorlds = new ArrayList<String>();
	// 設定ここまで

	/**
	 * コンストラクタ
	 * @param plugin JavaPlugin
	 */
	public ConfigurationManager(final JavaPlugin plugin){
		this.plugin = plugin;
		pluginDir = this.plugin.getDataFolder();
	}

	/**
	 * 必要なディレクトリ群を作成する
	 */
	private void createDirs(){
		createDir(plugin.getDataFolder());
	}

	/**
	 * 設定ファイルを読み込む
	 */
	public void loadConfig(boolean initialLoad){
		// ディレクトリ作成
		createDirs();

		// 設定ファイルパス取得
		File file = new File(pluginDir, "config.yml");
		String filepath = file.getPath();
		// 設定ファイルが見つからなければデフォルトのファイルをコピー
		if (!file.exists()){
			extractResource("/config.yml", pluginDir, false, true);
			log.info(logPrefix+ "config.yml is not found! Created default config.yml!");
		}

		plugin.reloadConfig();

		// 項目取得

		/* Basic Configs */
		msg = plugin.getConfig().getString("Message", defaultMsg);

		String blockLevelStr = plugin.getConfig().getString("BlockLevel", "0");
		try {
			blockLevel = Byte.parseByte(blockLevelStr);
		}catch (NumberFormatException ex){
			log.warning(logPrefix+ "BlockLevel could not cast to byte object!");
			blockLevel = 0;
		}

		onlyStones = plugin.getConfig().getBoolean("OnlyStones");

		bypassWorlds = plugin.getConfig().getStringList("BypassWorlds");
	}

	/**
	 * 設定ファイルに設定を書き込む
	 * @throws Exception
	 */
	public void save() throws Exception{
		plugin.saveConfig();
	}

	/**
	 * 存在しないディレクトリを作成する
	 * @param dir File 作成するディレクトリ
	 */
	private static void createDir(File dir){
		// 既に存在すれば作らない
		if (dir.isDirectory()){
			return;
		}
		if (!dir.mkdir()){
			log.warning(logPrefix+ "Can't create directory: " + dir.getName());
		}
	}

	/**
	 * リソースファイルをファイルに出力する
	 * @param from 出力元のファイルパス
	 * @param to 出力先のファイルパス
	 * @param force jarファイルの更新日時より新しいファイルが既にあっても強制的に上書きするか
	 * @param checkenc 出力元のファイルを環境によって適したエンコードにするかどうか
	 * @author syam
	 */
	static void extractResource(String from, File to, boolean force, boolean checkenc){
		File of = to;

		// ファイル展開先がディレクトリならファイルに変換、ファイルでなければ返す
		if (to.isDirectory()){
			String filename = new File(from).getName();
			of = new File(to, filename);
		}else if(!of.isFile()){
			log.warning(logPrefix+ "not a file:" + of);
			return;
		}

		// ファイルが既に存在して、そのファイルの最終変更日時がJarファイルより後ろなら、forceフラグが真の場合を除いて返す
		if (of.exists() && of.lastModified() > getJarFile().lastModified() && !force){
			return;
		}

		OutputStream out = null;
		InputStream in = null;
		InputStreamReader reader = null;
		OutputStreamWriter writer =null;
		DataInputStream dis = null;
		try{
			// jar内部のリソースファイルを取得
			URL res = LightLevelBlocker.class.getResource(from);
			if (res == null){
				log.warning(logPrefix+ "Can't find "+ from +" in plugin Jar file");
				return;
			}
			URLConnection resConn = res.openConnection();
			resConn.setUseCaches(false);
			in = resConn.getInputStream();

			if (in == null){
				log.warning(logPrefix+ "Can't get input stream from " + res);
			}else{
				// 出力処理 ファイルによって出力方法を変える
				if (checkenc){
					// 環境依存文字を含むファイルはこちら環境

					reader = new InputStreamReader(in, "UTF-8");
					writer = new OutputStreamWriter(new FileOutputStream(of)); // 出力ファイルのエンコードは未指定 = 自動で変わるようにする

					int text;
					while ((text = reader.read()) != -1){
						writer.write(text);
					}
				}else{
					// そのほか

					out = new FileOutputStream(of);
					byte[] buf = new byte[1024]; // バッファサイズ
					int len = 0;
					while((len = in.read(buf)) >= 0){
						out.write(buf, 0, len);
					}
				}
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}finally{
			// 後処理
			try{
				if (out != null)
					out.close();
				if (in != null)
					in.close();
				if (reader != null)
					reader.close();
				if (writer != null)
					writer.close();
			}catch (Exception ex){}
		}
	}

	public static File getJarFile(){
		return new File("plugins","LightLevelBlocker.jar");
	}

}