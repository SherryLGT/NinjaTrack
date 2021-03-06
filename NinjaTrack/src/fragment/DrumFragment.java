package fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import model.Song;
import nyp.fypj.ninjatrack.R;
import adapter.SongListAdapter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DrumFragment extends Fragment {
	
	private ProgressBar progress_bar;
	private TextView start_time, end_time;
	private static ListView lv_song;
	
	private static ArrayList<Song> songList;
	private static SongListAdapter adapter;
	
	public DrumFragment(){}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_instrument, container, false);
        progress_bar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        start_time = (TextView) rootView.findViewById(R.id.start_time);
        end_time = (TextView) rootView.findViewById(R.id.end_time);
        lv_song = (ListView) rootView.findViewById(R.id.lv_song);
        
        // Retrieve all the songs and sort accordingly
        getSongList();
        Collections.sort(songList, new Comparator<Song>() {
			@Override
			public int compare(Song a, Song b) {
				return a.getTitle().compareTo(b.getTitle());
			}
        });

        adapter = new SongListAdapter(getActivity(), songList);
        lv_song.setAdapter(adapter);
        
        progress_bar.setProgress(20);
        start_time.setText("0:00");
        end_time.setText("3:40");
         
        return rootView;
    }
	
	private void getSongList() {
		songList = new ArrayList<Song>();
		String[] songNames;
		
		AssetManager assetManager = getActivity().getAssets();
		try {
			songNames = assetManager.list("drum");
			for(int i = 0; i < songNames.length; i++) {
				String title = songNames[i];
				AssetFileDescriptor data = getActivity().getAssets().openFd("drum/" + songNames[i]);				
				MediaPlayer mediaPlayer = new MediaPlayer();
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				mediaPlayer.setDataSource(data.getFileDescriptor(), data.getStartOffset(), data.getLength());
				mediaPlayer.prepare();
				int duration = mediaPlayer.getDuration();
				songList.add(new Song(title, duration, data, false));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Handler handler = new Handler(){
		public void handleMessage(Message msg){
			String title = msg.getData().getString("title");
			boolean isPlaying = msg.getData().getBoolean("isPlaying");
			ManipulateUI(title, isPlaying);
		}
	};
	
	private static void ManipulateUI(String title, boolean isPlaying){
		int position = 0;
		for(Song song : songList) {
			if(title.equals(song.getTitle())) {
				View view = adapter.getView(adapter.getPosition(song), null, lv_song);
				TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
				ImageView btn_pp = (ImageView) view.findViewById(R.id.btn_pp);
				if(tv_title.getText() == song.getTitle()){
					lv_song.smoothScrollToPosition(position);
					if(isPlaying){
						song.setPlaying(true);
						btn_pp.setImageDrawable(view.getResources().getDrawable(R.drawable.btn_pause));
					}
					else{
						song.setPlaying(false);
						btn_pp.setImageDrawable(view.getResources().getDrawable(R.drawable.btn_pause));
					}
				}
			}
			position++;
		}
		adapter.notifyDataSetChanged();
	}
}
