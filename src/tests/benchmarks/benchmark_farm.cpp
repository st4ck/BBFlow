#include <iostream>
#include <ff/ff.hpp>
using namespace ff;

struct Emitter: ff_monode_t<long> {
	int ntask;

    long *svc(long*) {
        size_t n = get_num_outchannels();
        for(int i=1;i<=ntask;++i) {
			long *t;
			t = (long*)malloc(sizeof(long));
			*t = i;
            ff_send_out_to(t, i % n);
		}
        return EOS;
    }
};
struct Worker1: ff_node_t<long> {
    long *svc(long *in) {
		//usleep(5000);

		for (int i=0; i<1000000; i++) {
			*in = (*in)*1000;
			*in = (*in)/999;
		}
        return in;
    }
};

struct Collector: ff_minode_t<long> {
	long *svc(long *in) {
        return GO_ON;
    }
};

int main(int argc, char *argv[]) {
	int streamlen = 1000;
	int n_workers = 16;

    if (argc == 3) {
		streamlen=atoi(argv[1]);
		n_workers=atoi(argv[2]);
	}

    Emitter E;
	Collector C;

	E.ntask = streamlen;

    std::vector<std::unique_ptr<ff_node>> W;
    for(int i=0;i<n_workers;++i)
        W.push_back(make_unique<Worker1>());

    ff_Farm<> farm(std::move(W), E, C);

	unsigned long inizio=getusec();
	if (farm.run()<0) {
        error("running farm\n");
        return -1;
    }
    if (farm.wait_collector()<0) {
        error("waiting termination\n");
        return -1;
    }

	unsigned long fine=getusec();
    std::cout << "TEST  Time = " << (fine-inizio) / 1000.0 << " ms\n";

    return 0;
}