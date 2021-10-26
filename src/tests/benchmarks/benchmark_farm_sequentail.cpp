#include <iostream>

int main(int argc, char* argv[]) {
	int ntask = 1000;

    if (argc == 2) {
		ntask=atoi(argv[1]);
	}

	long *t;
	for(int i=1;i<=ntask;++i) {
		t = (long*)malloc(sizeof(long));
		*t = i;

		for (int j=0; j<1000000; j++) {
			*t = (*t)*1000;
			*t = (*t)/999;
		}
	}

	std::cout << *t << std::endl;

	return 0;
}