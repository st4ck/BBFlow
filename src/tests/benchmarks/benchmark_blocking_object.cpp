#include <iostream>
#include <ff/ff.hpp>
using namespace ff;

class myTask_t {
    public:
	    long num;
};

struct Emitter: ff_node_t<myTask_t> {
    int ntask;

    myTask_t *svc(myTask_t*) {
        for(long i=1;i<=ntask;++i)  {
            myTask_t* t = new myTask_t;
			t->num = i;
            ff_send_out(t);
		}
        return EOS;
    }
};

struct Filter2: ff_node_t<myTask_t> {
        myTask_t *svc(myTask_t *in) {
			in->num *= 2;
			return GO_ON;
		}
};

int main(int argc, char * argv[]) {
    int streamlen = 1000;

    if (argc > 1)
    streamlen=atoi(argv[1]);

    Emitter E;
    std::cout << streamlen << std::endl;
    E.ntask = streamlen;
    Filter2 f2;

    ff_Pipe<> pipe(E, f2);

	unsigned long inizio=getusec();

    if (pipe.run_and_wait_end()<0) {
        error("running pipe\n");
        return -1;
    }

    unsigned long fine=getusec();
    std::cout << "TEST  Time = " << (fine-inizio) / 1000.0 << " ms\n";


    return 0;
}
