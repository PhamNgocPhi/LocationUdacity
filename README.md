public class MapViewFragment extends Fragment implements OnMapReadyCallback {

    private Context context;
    private int tabPosition;
    private ApiUtils getClient;
    private AppDatabase db;
    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean isLoaded = false, isVisibleToUser;
    private ArrayList<Marker> markerArrayList = new ArrayList<>();

    public MapViewFragment() {
        // Required empty public constructor
    }

    public static Fragment getInstance(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("tabPosition", position);
        MapViewFragment mapViewFragment = new MapViewFragment();
        mapViewFragment.setArguments(bundle);
        return mapViewFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_map_view, container, false);

        mapView = rootView.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        //mapView.onResume();
        MapsInitializer.initialize(context);
        if (isVisibleToUser && !isLoaded) {
            mapView.getMapAsync(this);
            isLoaded = true;
        }
        tabPosition = getArguments().getInt("tabPosition");
        getClient = RetrofitClient.getBaseService().create(ApiUtils.class);
        db = AppDatabase.getInMemoryDatabase(context);
        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;
        if (isVisibleToUser && isAdded() && !isLoaded) {
            mapView.getMapAsync(this);
            isLoaded = true;
        }
    }

    private void getStudioOnMap() {
        Log.i("loadData","loadData");
        Map<String, String> mapParamRetrofit = RequestUtils.requestGetSessionList(tabPosition, context);
        getClient.getStudioFilter(TOKEN,mapParamRetrofit)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    List<Integer> listStudioId = new ArrayList<>(result.getResult().size());
                    listStudioId.addAll(result.getResult().keySet());
                    if(listStudioId != null && listStudioId.size() > 0) {
                        //addMarker(listStudioId);
                        addMarkerDefault(listStudioId);
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                    Toast.makeText(context, "get studio on map error", Toast.LENGTH_SHORT).show();
                });
    }

    //add marker use map utils library
    private void addMarker(List<Integer> listStudioId) {
        ClusterManager<StudioForMap> clusterManager = new ClusterManager<StudioForMap>(context, googleMap);
        googleMap.setOnCameraIdleListener(clusterManager);
        googleMap.setOnMarkerClickListener(clusterManager);
        db.studioDao().findStudioForMapByListId(listStudioId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    clusterManager.addItems(result);
                }, throwable -> {
                    throwable.printStackTrace();
                    Toast.makeText(context, "get studio for map error", Toast.LENGTH_SHORT).show();
                });
    }

    private void addMarkerDefault(List<Integer> listStudioId) {
        removeMarkers();
        googleMap.clear();
        db.studioDao().findStudioForMapByListId(listStudioId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    Log.i("result", "size: "+result.size());
                    for(StudioForMap studio : result) {
                        MarkerOptions markerOptions = new MarkerOptions().position(studio.getPosition())
                                .title(studio.getName())
                                .icon(BitmapDescriptorFactory.fromBitmap(getIcon(studio)));
                        Marker marker = googleMap.addMarker(markerOptions);
                        marker.setTag(studio);
                        markerArrayList.add(marker);
                    }
                    Log.i("result", "add done");
                }, throwable -> {
                    throwable.printStackTrace();
                    Toast.makeText(context, "get studio for map error", Toast.LENGTH_SHORT).show();
                });
    }

    private void removeMarkers() {
        if(markerArrayList != null && !markerArrayList.isEmpty()) {
            for (Marker marker: markerArrayList) {
                marker.remove();
            }
            markerArrayList.clear();
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("we need permission")
                        .setMessage("khong cap quyen app loi tu chiu")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(), new String[]
                                        {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        googleMap.setMyLocationEnabled(true);
                    }
                } else {
                }
                return;
            }

        }
    }

    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            CameraPosition position = CameraPosition.builder()
                                    .target(new LatLng(location.getLatitude(),
                                            location.getLongitude()))
                                    .zoom(15f)
                                    .bearing(0.0f)
                                    .tilt(0.0f)
                                    .build();
                            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));

                            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                            googleMap.setTrafficEnabled(true);
                            googleMap.setMyLocationEnabled(true);
                        }
                    }
                });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i("map", "load map: " + tabPosition);
        this.googleMap = googleMap;

        if (checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Request location updates:
                googleMap.setMyLocationEnabled(true);
                getStudioOnMap();
                getDeviceLocation();
            }
            else {
                getStudioOnMap();
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(20.997521, 105838964), 15));
            }
        }

        googleMap.setOnInfoWindowClickListener(marker -> {
            StudioForMap studio = (StudioForMap) marker.getTag();
            Intent intent = new Intent(context, StudioDetailActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Constant.STUDIO_ID, studio.getId());
            startActivity(intent);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Destroy", "destroy tab " + tabPosition);
        if (db != null)
            db.destroyInstance();
        if (mapView != null)
            mapView.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private Bitmap getIcon(StudioForMap room) {
        BitmapDrawable bitmapDrawable;
        if (room.getFitnessTypeCodes().size() < 2 && room.getFitnessTypeCodes().size() > 0) {
            switch (room.getFitnessTypeCodes().get(0).toLowerCase()) {
                case "m": {
                    bitmapDrawable = (BitmapDrawable)getResources().getDrawable(R.drawable.ma);
                    break;
                }
                case "a": {
                    bitmapDrawable = (BitmapDrawable)getResources().getDrawable(R.drawable.aerobic);
                    break;
                }
                case "g": {
                    bitmapDrawable = (BitmapDrawable)getResources().getDrawable(R.drawable.gym);
                    break;
                }
                case "k": {
                    bitmapDrawable = (BitmapDrawable)getResources().getDrawable(R.drawable.boxing);
                    break;
                }
                case "d": {
                    bitmapDrawable = (BitmapDrawable)getResources().getDrawable(R.drawable.dance);
                    break;
                }
                case "z": {
                    bitmapDrawable = (BitmapDrawable)getResources().getDrawable(R.drawable.zumba);
                    break;
                }
                case "b": {
                    bitmapDrawable = (BitmapDrawable)getResources().getDrawable(R.drawable.aerobic);
                    break;
                }
                default: {
                    bitmapDrawable = (BitmapDrawable)getResources().getDrawable(R.drawable.other);
                    break;
                }
            }
        } else {
            switch (room.getFitnessTypeCodes().size()) {
                case 2: {
                    bitmapDrawable = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_2);
                    break;
                }
                case 3: {
                    bitmapDrawable = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_3);
                    break;
                }
                case 4: {
                    bitmapDrawable = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_4);
                    break;
                }
                case 5: {
                    bitmapDrawable = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_5);
                    break;
                }
                case 6: {
                    bitmapDrawable = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_6);
                    break;
                }
                default: {
                    bitmapDrawable = (BitmapDrawable)getResources().getDrawable(R.drawable.other);
                    break;
                }
            }
        }
        return Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(),80,80,false);
    }

}